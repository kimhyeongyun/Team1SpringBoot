package com.mycompany.webapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Pager;
import com.mycompany.webapp.dto.Product;
import com.mycompany.webapp.service.ProductsService;

@RestController
@RequestMapping("/products")
public class ProductsController {
   @Autowired
   private ProductsService productsService;
   
   @GetMapping("")
   public Map<String, Object> list(@RequestParam(defaultValue="1") int pageNo) {
      int totalRows = productsService.getCount();
      Pager pager = new Pager(5, 5, totalRows, pageNo);
      List<Product> list = productsService.getList(pager);
      Map<String, Object> map = new HashMap<>();
      map.put("products", list);
      map.put("pager", pager);
      return map;
   }   
   
   @PostMapping("")
   // @RequestBody : 요청 http 본문에 json이 포함되어 있을 경우 raw -> type:json // 서버에 따라 @RequestBody로 받거나 안받거나 결정됨.
   public Product create(@RequestBody Product product) {
      productsService.insert(product);
      return product;
   }
   
   @GetMapping("/{pid}")
   public Product read(@PathVariable int pid) {
      Product product = productsService.getProduct(pid);
      return product;
   }   
   
   @PutMapping("")
   // @RequestBody : 요청 http 본문에 json이 포함되어 있을 경우 raw type:json
   public Product update(@RequestBody Product product) { 
      productsService.update(product);
      return product;
   }
   
   @DeleteMapping("/{pid}")
   public void delete(@PathVariable int pid) {
      productsService.delete(pid);
   }
}