package com.example.demo1228_2.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.dto.BuylistDto;
import com.example.demo1228_2.entity.Buylist;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.entity.User;
import com.example.demo1228_2.mapper.BuylistMapper;
import com.example.demo1228_2.mapper.ProductMapper;
import com.example.demo1228_2.service.IBuylistService;
import com.example.demo1228_2.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/buylist")
@Slf4j // 自动生成log对象
public class BuylistController {
    @Autowired
    BuylistMapper buylistMapper;
    @Autowired
    IBuylistService buylistService;

    @Autowired
    ProductMapper productmapper;

    @Autowired
    IProductService productService;

    @PostMapping("/add") // 加购物车
    public R<String> AddToBuyList(@RequestBody Buylist buylist, HttpSession session){
        // 参数无商品号 返回错误
        if(buylist.getProduct_id()==0)return R.error("商品号缺失");
        // 用session赋值User_id
        buylist.setUser_id(Long.parseLong(session.getAttribute("IsLogin").toString()));
        // 根据商品号查找商品
        Product product = productmapper.selectById(buylist.getProduct_id());

         // 商品不存在 不加购
        if(product==null)return R.error("商品号不存在或已被删除");


        // 购物车有无同商品id订单
        try{
            LambdaQueryWrapper<Buylist> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Buylist::getProduct_id,buylist.getProduct_id())
                        .eq(Buylist::getUser_id,buylist.getUser_id());
            Buylist db_buylist = buylistMapper.selectOne(queryWrapper);
            //LambdaUpdateWrapper<Buylist> u = new LambdaUpdateWrapper<>();
            // 如有
            if(db_buylist!=null){
                // 数量加上原数量
                db_buylist.setProduct_num(db_buylist.getProduct_num()+buylist.getProduct_num());
                // 时间更新
                db_buylist.setCreate_time(buylist.getCreate_time());
                // 更新是否成功
                if(buylistMapper.updateById(db_buylist)!=0){
                    return R.success("购物车成功添加"+buylist.getProduct_num()+"件商品");
                }else{
                    return R.error("添加失败，返回0条");
                }
            }
        }catch (Exception e){
            return R.error("错误，可能是数据库多个重复订单，或者更新原订单错误："+e.getMessage());
        }


        // 向buylist表插入（如果前面没订单）
        int res=0;
        try{
            res = buylistMapper.insert(buylist);
        }catch(Exception e){
            return R.error("异常："+e.getMessage());
        }
        if(res!=0)return R.success("购物车成功添加"+buylist.getProduct_num()+"件商品");
        else return R.error("添加失败，返回0条");
    }

    @GetMapping("/page") // 分页查询 接收params //防空设默认
    public R<Page<BuylistDto>> FindPageBuylist(@RequestParam(defaultValue = "-1")int currentPage,
                                      @RequestParam(defaultValue = "-1")int PageSize, HttpSession session) {
        try {
            // 空参数抛异常
            if (currentPage == -1 || PageSize == -1) throw new CustomException("分页查询参数为空");
            // 分页查询
            Page<Buylist> page = new Page<>(currentPage, PageSize);

            // 创建LambdaQueryWrapper实例
            LambdaQueryWrapper<Buylist> queryWrapper = new LambdaQueryWrapper<>();
            // 根据时间从高到低排序
            queryWrapper.orderByDesc(Buylist::getCreate_time);
            // 根据权限查询
            if(session.getAttribute("Role")==null || !Objects.equals(session.getAttribute("Role").toString(), "admin"))
                // 权限不足 只能查自己
                queryWrapper.eq(Buylist::getUser_id,session.getAttribute("IsLogin").toString());

            // 执行查询
            Page<Buylist> res = buylistMapper.selectPage(page, queryWrapper);
            // 获取Buylist集合
            List<Buylist> buylists = res.getRecords();
            log.info("buylists集合：{}",buylists);
            // 获取product id集合
            List<Long> productIds = new ArrayList<>();
            for(Buylist buylist : buylists){
                // 存在就不重复添加
                if(productIds.contains(buylist.getProduct_id()))continue;
                else  productIds.add(buylist.getProduct_id());
            }
            log.info("id集合：{}",productIds);
            // 获取product合集
            List<Product> products = productService.listByIds(productIds);
            log.info("product合集：{}",products);
            // 搞成id为索引的map
            Map<Long,Product> productMap = new HashMap<>();
            for(Product product : products){
                productMap.put(product.getId(),product);
            }
            List<BuylistDto> buylistDtos = new ArrayList<>();
            for(Buylist buylist : buylists){
                BuylistDto buylistDto = new BuylistDto();
                buylistDto.setBuylist(buylist);
                buylistDto.setProduct(productMap.get(buylist.getProduct_id()));
                buylistDtos.add(buylistDto);
            }
            // 创建一个新的 Page 对象
            Page<BuylistDto> resDto = new Page<>(res.getCurrent(), res.getSize());
            resDto.setTotal(res.getTotal());

            // 设置新的记录
            resDto.setRecords(buylistDtos);
            /*
            //控制台打印json
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(res);
            System.out.println(json);
            //
            */
            log.info("分页查询成功");
            log.info("resDto集合：{}",resDto.getRecords());
            return R.success(resDto);
        } catch (Exception e) {
            log.info("分页查询失败：{}", e.getMessage());
            return R.error(e.getMessage());
        }
    }
    @PostMapping("/update") // 更新购物车 // 1.id存在 2.userid是本人 3. num不为负数 4.更新num数量
    public R<String> UpdateBuylist(@RequestBody List<BuylistDto> buylistdtos, HttpSession session){
        // 获取订单合集 //构建Map (id,buylist)
        List<Buylist> buylists = new ArrayList<>();
        Map<Long,Buylist> buylistNumMap = new HashMap<>();
        for(BuylistDto buylistdto : buylistdtos){
            buylists.add(buylistdto.getBuylist());
            buylistNumMap.put(buylistdto.getBuylist().getId(),buylistdto.getBuylist());
        }
        // 获取订单id合集
        List<Long> buylistIds = new ArrayList<>();
        for(BuylistDto buylistdto : buylistdtos){
            // 去重
            if(!buylistIds.contains(buylistdto.getBuylist().getId()))
                buylistIds.add(buylistdto.getBuylist().getId());
        }
        try{
            // 根据ids 获取数据库订单(防伪造)
            List<Buylist> legal_buylists = buylistService.listByIds(buylistIds);
            log.info("buylistIds:{}",buylistIds);
            // 对获取的数据库订单赋值num
            for(Buylist legal_buylist : legal_buylists){
                // 必须是本人订单 才能修改
                if(legal_buylist.getUser_id()!=Integer.parseInt(session.getAttribute("IsLogin").toString()))
                    throw new CustomException("不是本人订单");
                // 3.num不为负数
                if(legal_buylist.getProduct_num()<1)
                    throw new CustomException("商品数量小于1");
                // 根据Map的id索引设置num
                legal_buylist.setProduct_num(buylistNumMap.get(legal_buylist.getId()).getProduct_num());
                // 根据Map的id索引设置is_selected
                legal_buylist.set_selected(buylistNumMap.get(legal_buylist.getId()).is_selected());
            }

            // null或没有不会设置空 而是略(但是为啥我的product_id被化成0)
            // int值不设置默认设置0 然后就被设置0
            log.info("购物车批处理{}",legal_buylists);
            if(buylistService.updateBatchById(legal_buylists))return R.success("购物车更新批处理成功");
            else return R.error("购物车更新批处理失败");
        }catch (Exception e){
            return R.error("购物车更新异常："+e.getMessage());
        }
    }

    @DeleteMapping("/deletebyid") // 根据id删一个
    public R<String> DeleteById(@RequestParam(defaultValue = "-1")int D_id, HttpSession session){
        try{
            // 查要删的是不是属于客户
            Buylist buylist = buylistService.getById(D_id);
            if(buylist==null)return R.error("商品号对应的商品不存在");
            // 比较客户号和session id
            if(buylist.getUser_id()==Integer.parseInt(session.getAttribute("IsLogin").toString())){
                if(buylistService.removeById(D_id))return R.success("删除成功");
                else return R.error("删除失败");
            }else{
                return R.error("不能删除非自己的商品");
            }
        }catch(Exception e){
            return R.error("异常："+e.getMessage());
        }
    }

    @DeleteMapping("/deletebyids") // 根据ids删一堆
    public R<String> DeleteByIds(@RequestBody List<Long> D_ids, HttpSession session){
        if (D_ids == null || D_ids.isEmpty()) {
            return R.error("无效的请求参数");
        }

        try{
            List<Buylist> buylists = buylistService.listByIds(D_ids);
            for(Buylist buylist:buylists){
                // 查要删的是不是属于客户
                if(buylist.getUser_id()!=Integer.parseInt(session.getAttribute("IsLogin").toString()))
                    throw new CustomException("不是本人订单");
            }
            log.info("批删除："+buylists);
            if(buylistService.removeByIds(D_ids))return R.success("购物车批删除成功");
            else return R.error("购物车批删除失败");
        }catch(Exception e){
            return R.error("异常："+e.getMessage());
        }
    }
}
