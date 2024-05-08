package com.example.demo1228_2.service.impl;

import com.example.demo1228_2.entity.Comment;
import com.example.demo1228_2.mapper.CommentMapper;
import com.example.demo1228_2.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yjz
 * @since 2024-05-07
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

}
