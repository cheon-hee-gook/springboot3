package com.example.board.service;

import com.example.board.model.Board;
import com.example.board.model.Comment;
import com.example.board.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {
    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // 댓글 계층 구조 생성
    public List<Comment> getCommentHierarchy(Board board) {
        List<Comment> allComments = commentRepository.findByBoard(board);

        // 부모 ID별로 댓글 그룹화
        Map<Long, List<Comment>> commentMap = new HashMap<>();
        for (Comment comment : allComments) {
            Long parentId = (comment.getParent() == null) ? null : comment.getParent().getId();
            commentMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(comment);
        }

        // 계층 구조로 변환
        List<Comment> result = new ArrayList<>();
        buildHierarchy(null, commentMap, result, 0);

        return result;
    }

    // 계층 구조를 재귀적으로 빌드
    private void buildHierarchy(Long parentId, Map<Long, List<Comment>> commentMap, List<Comment> result, int depth) {
        List<Comment> comments = commentMap.get(parentId);
        if (comments != null) {
            for (Comment comment : comments) {
                comment.setDepth(depth); // 계층 깊이 설정
                result.add(comment);    // 현재 댓글 추가
                buildHierarchy(comment.getId(), commentMap, result, depth + 1); // 자식 댓글 추가
            }
        }
    }

}
