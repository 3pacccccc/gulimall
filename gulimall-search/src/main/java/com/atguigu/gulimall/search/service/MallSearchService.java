package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * @author: maruimin
 * @date: 2020/5/7 20:34
 */
public interface MallSearchService {
    SearchResult search(SearchParam param);
}
