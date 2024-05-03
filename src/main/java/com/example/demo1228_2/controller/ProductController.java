package com.example.demo1228_2.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.GlobalProperties;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.config.Tool;
import com.example.demo1228_2.entity.*;
import com.example.demo1228_2.mapper.DataResultMapper;
import com.example.demo1228_2.mapper.ProductMapper;
import com.example.demo1228_2.mapper.ProductRateMapper;
import com.example.demo1228_2.service.impl.ProductServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/product")
@Slf4j // 自动生成log对象
public class ProductController {
    @Autowired
    ProductMapper productmapper;

    @Autowired
    DataResultMapper dataResultMapper;

    @Autowired
    ProductRateMapper productRateMapper;

    @Autowired
    GlobalProperties globalProperties;

    @GetMapping("/page") // 分页查询 接收params //防空设默认
    public R<Page<Product>> FindPageProduct(@RequestParam Map<String,String> params){
        //log.info("!!:{}",FType);
        try {

            int currentPage = Integer.parseInt(params.get("currentPage"));
            int PageSize = Integer.parseInt(params.get("PageSize"));
            String FName = params.get("FName");
            String FType = params.get("FType");
            String value2 = params.get("value2");

            LambdaQueryChainWrapper<Product> query = new LambdaQueryChainWrapper<>(productmapper);

            // 空参数抛异常
            if(currentPage == 0 || PageSize == 0 )throw new CustomException("分页查询参数为空");
            // 分页查询
            Page<Product> page = new Page<>(currentPage, PageSize);

            // FName不为空 筛选名字
            if(FName != null)query.like(Product::getName,FName);
            // FType不为空 筛选种类
            if(FType != null)query.eq(Product::getType,FType);

            if(value2 != null){
                PageQueryValue2(query, value2);
            }




            // 执行查询
            Page<Product> res = query.page(page);

            /*
            //控制台打印json
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(res);
            System.out.println(json);
            //
            */
            log.info("分页查询成功");
            // 访客加一
            DataResult dataResult = dataResultMapper.selectById(45698);
            dataResult.setHome_visitors(dataResult.getHome_visitors()+1);
            dataResultMapper.updateById(dataResult);

            return R.success(res).add("home_visitors",dataResult.getHome_visitors());
        }catch(Exception e){
            log.info("分页查询失败：{}",e.getMessage());
            return R.error(e.getMessage());
        }


    }

    @GetMapping("/getone") // 查一个商品
    public R<Product> FindOneProduct(@RequestParam(defaultValue = "-1")Long id){
        Product product;
        if(id ==-1)return R.error("id为空");
        try{
            product = productmapper.selectById(id);
            if(product != null){
                // 浏览量加一
                product.setVisited_num(product.getVisited_num()+1);
                productmapper.updateById(product);
            }
        }catch(Exception e){
            log.info("查一个商品失败：{}",e.getMessage());
            return R.error(e.getMessage());
        }

        //log.info("{},{}",averageRate,productRateList.size());
        return R.success(product).add("rate_value",product.getRate())
                .add("rate_num",product.getRate_num());
    }

    /**
     * 添加商品
     * @param photo 图片
     * @param product_json product字符串对象
     */
    @PostMapping("/addonebyadmin") // 加一个商品 //不加注解默认form-data
    public R<String> FindOneProduct(MultipartFile photo,String product_json,HttpSession session){
        try{

            //log.info(photo.getOriginalFilename());
            //log.info(photo.getContentType());
            //log.info(product_json);
            ObjectMapper objectMapper = new ObjectMapper();
            // 序列化
            Product product = objectMapper.readValue(product_json, Product.class);
            log.info("{}",product);
            // 反序列化
            //String json_str = objectMapper.writeValueAsString(product);
            //log.info(json_str);



            // 验证权限
            if(!session.getAttribute("Role").toString().equals("admin"))
                throw new CustomException("不是管理员，禁止操作");

            // 防空
            if(product.getName()==null || product.getPrice()==null)
                throw new CustomException("商品名或价格不能为空");

            //名字防重复
            if(Db.lambdaQuery(Product.class).eq(Product::getName,product.getName()).one()!=null)
                throw new CustomException("名字已存在，添加失败");


            // 存图
            //Tool.saveFile(photo,Tool.PHOTO_SAVE_URL);
            // 转换webp存图
            String name = "noproduct";
            if(photo!=null)name = Tool.convertToWebp(photo);
            log.info(name.replace(".webp", ""));
            //log.info("是否有图片 名字：{}",Tool.checkFileExists(name));

            product.setId(null);
            product.setVersion(null);
            product.setCreate_time(null);
            product.setUser_id(Long.parseLong(session.getAttribute("IsLogin").toString()));
            if(product.getType()==null)product.setType("0");
            product.setPhoto(name.replace(".webp", ""));
            if(productmapper.insert(product)==0){
                // 指定要删除的文件路径
                String filePath = Tool.PHOTO_SAVE_URL + name; // 替换为实际的文件路径
                File file = new File(filePath);
                // 尝试删除文件
                boolean deleted = file.delete();
                throw new CustomException("输入库插入失败");
            }else{
                return R.success("插入成功");
            }

        }catch(Exception e){
            return R.error(e.getMessage());
        }
    }

    @PutMapping("/updateonebyadmin") // 更新一个商品 //不加注解默认form-data
    public R<String> UpdateOneProduct(MultipartFile photo,String product_json,HttpSession session){
        try{

            ObjectMapper objectMapper = new ObjectMapper();
            // 序列化
            Product product = objectMapper.readValue(product_json, Product.class);
            log.info("{}",product);
            // 反序列化
            //String json_str = objectMapper.writeValueAsString(product);
            //log.info(json_str);

            // 验证权限
            if(!session.getAttribute("Role").toString().equals("admin"))
                throw new CustomException("不是管理员，禁止操作");

            // 防空
            if(product.getName()==null || product.getPrice()==null)
                throw new CustomException("商品名或价格不能为空");

            Product db_product = productService.getById(product.getId());

            // 改名了，就检查名字防重复
            if(!db_product.getName().equals(product.getName()) && Db.lambdaQuery(Product.class).eq(Product::getName,product.getName()).one()!=null)
                throw new CustomException("名字已存在，添加失败");


            // 有删除（或者本身是noproduct）
            if(product.getPhoto().equals("noproduct")){

                // 无传图
                if(photo==null){
                    // 本身不是noproduct（前端删除请求）
                    if(!db_product.getPhoto().equals("noproduct"))
                        // 删老的图
                        Tool.deleteOneWebp(db_product.getPhoto()+".webp");

                    // 本身是 啥也不做
                }
                // 有传图 // 传图必是noproduct
                else{
                    // 删老的图(不是noproduct.webp的话,别把默认图删了)
                    if(!db_product.getPhoto().equals("noproduct"))Tool.deleteOneWebp(db_product.getPhoto()+".webp");
                    // 存新图
                    String new_photo_name = Tool.convertToWebp(photo);
                    // 改名
                    product.setPhoto(new_photo_name.replace(".webp",""));

                    log.info("删 存 改名");
                }

            }
            // 图片名字是正常文字（啥也不做）

            if(!productService.updateById(product))
                throw new CustomException("updateById失败");

            return R.success("更新成功");



        }catch(Exception e){
            return R.error(e.getMessage());
        }
    }

    @Autowired
    ProductServiceImpl productService;

    /**
     * 删除商品
     * @param productList 要删除的商品列表
     */
    @PostMapping("/deletelistbyadmin") // 删一堆商品
    public R<String> FindOneProduct(@RequestBody List<Product> productList,HttpSession session) {
        try{

            // 验证权限
            if(!session.getAttribute("Role").toString().equals("admin"))
                throw new CustomException("不是管理员，禁止操作");

            // 防空
            if(productList==null)
                throw new CustomException("参数不能为空");

            List<Long> IdList = new ArrayList<>();
            for(Product product:productList)
                if(product.getId()!=null){
                    IdList.add(product.getId());
                    Tool.deleteOneWebp(product.getPhoto()+".webp"); // 删图片
                }

            if(!productService.removeByIds(IdList))
                throw new CustomException("数据库删除失败，可能存在非法ID");

            // 删图片

            return R.success("删除成功");

        }catch (Exception e){
            return R.error(e.getMessage());
        }

    }

    @GetMapping("/selectpagebyadmin") // 分页查询 接收params //防空设默认
    public R<Page<Product>> FindPageProducts(@RequestParam Map<String, String> params, HttpSession session){
        // 使用LambdaQueryChainWrapper构建查询
        LambdaQueryChainWrapper<Product> query = new LambdaQueryChainWrapper<>(productmapper);

        // 根据条件动态添加查询条件
        if (params.get("name") != null) {
            query.like(Product::getName, params.get("name"));
        }
        // 单独处理startDate，如果存在则查询大于等于这个日期的记录
        if (params.get("startDate") != null) {
            query.ge(Product::getCreate_time, params.get("startDate")); // ge是“greater than or equal to”的缩写
        }
        // 单独处理endDate，如果存在则查询小于等于这个日期的记录
        if (params.get("endDate") != null) {
            query.le(Product::getCreate_time, params.get("endDate")); // le是“less than or equal to”的缩写
        }
        if (params.get("id") != null) {
            query.like(Product::getId, params.get("id"));
        }
        // 排序方式
        if (params.get("value2") != null) {
            String value2 = params.get("value2");
            PageQueryValue2(query, value2);
        }

        Page<Product> page = new Page<>(1,10);
        // 防空参数
        if(params.get("currentPage")!=null && params.get("PageSize")!=null)
            page = new Page<>(Long.parseLong(params.get("currentPage")),Long.parseLong(params.get("PageSize")));
        // 执行分页查询
        Page<Product> result = query.page(page);

        // map返回筛选
        R<Page<Product>> response = R.success(result);
        response.setMap(params);

        return response;
    }

    private void PageQueryValue2(LambdaQueryChainWrapper<Product> query, String value2) {
        switch(value2){
            case "a":
                break;
            case "b":
                query.orderByDesc(Product::getCreate_time);
                break;
            case "c":
                query.orderByDesc(Product::getVisited_num);
                break;
            case "d":
                query.orderByDesc(Product::getSold_num);
                break;
            case "e":
                query.orderByDesc(Product::getRate);
                break;
            default:
                break;
        }
    }
}
