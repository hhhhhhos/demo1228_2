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
import com.example.demo1228_2.service.impl.BuylistServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/buylist")
@Slf4j // 自动生成log对象
public class BuylistController {
    @Autowired
    BuylistMapper buylistMapper;
    @Autowired
    BuylistServiceImpl buylistService;

    @Autowired
    ProductMapper productmapper;

    @Autowired
    IProductService productService;

    @PostMapping("/add") // 加购物车
    public R<String> AddToBuyList(@RequestBody Buylist buylist, HttpSession session){
        try{
            // 加购1 失败抛异常
            return buylistService.saveOneOrUpdateOne(buylist,session);
        }catch (Exception e){
            return R.error("错误:"+e.getMessage());
        }

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

            //只能查自己
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
            // 获取product合集 // 防空sql报错
            List<Product> products = new ArrayList<>();
            if (!productIds.isEmpty()) {
                products = productService.listByIds(productIds);
            }
            log.info("product合集：{}",products);
            // 搞成id为索引的map
            Map<Long,Product> productMap = new HashMap<>();
            for(Product product : products){
                productMap.put(product.getId(),product);
            }
            // 被删商品构造
            Product nofound_product = new Product();
            nofound_product.setName("商品已被删除");
            nofound_product.setNum(0);
            nofound_product.setPhoto("deletedproduct");
            nofound_product.setPrice(BigDecimal.valueOf(0));

            List<BuylistDto> buylistDtos = new ArrayList<>();
            for(Buylist buylist : buylists){
                BuylistDto buylistDto = new BuylistDto();
                buylistDto.setBuylist(buylist);
                if(productMap.get(buylist.getProduct_id())!=null)
                    buylistDto.setProduct(productMap.get(buylist.getProduct_id()));
                else
                    buylistDto.setProduct(nofound_product);
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
        try{
            return buylistService.updateByList2(buylistdtos,session);
        }catch (Exception e){
            log.info("购物车更新批处理异常{}:",e.getMessage());
            return R.error("购物车更新异常："+e.getMessage());
        }
    }

    @DeleteMapping("/deletebyid") // 根据id删一个
    public R<String> DeleteById(@RequestParam(defaultValue = "-1")Long D_id, HttpSession session){
        try{
            // 查要删的是不是属于客户
            Buylist buylist = buylistService.getById(D_id);
            if(buylist==null)return R.error("商品号对应的商品不存在");
            // 比较客户号和session id
            if(buylist.getUser_id().equals(Long.parseLong(session.getAttribute("IsLogin").toString()))){
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
                if(!buylist.getUser_id().equals(Long.parseLong(session.getAttribute("IsLogin").toString())))
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
