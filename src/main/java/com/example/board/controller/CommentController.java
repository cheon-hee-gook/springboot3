package com.example.board.controller;

import com.example.board.config.SecurityConfig;
import com.example.board.model.Board;
import com.example.board.model.Comment;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/comments")
public class CommentController {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    public CommentController(CommentRepository commentRepository, BoardRepository boardRepository) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
    }

    // 댓글 작성
    @PostMapping("/{boardId}")
    public String addComment(
            @PathVariable Long boardId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: " + boardId));

        Comment comment = new Comment();
        comment.setBoard(board);
        comment.setContent(content);
        comment.setAuthor(auth.getName());

        if (parentId != null) { // 대댓글 처리
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다: " + parentId));
            comment.setParent(parentComment);
        }

        commentRepository.save(comment);
        return "redirect:/board/detail/" + boardId;
    }

    // 댓글 삭제
    @PostMapping("/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다: " + commentId));

        if (!comment.getAuthor().equals(auth.getName())) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        Long boardId = comment.getBoard().getId();
        commentRepository.delete(comment);
        return "redirect:/board/detail/" + boardId;
    }
}
