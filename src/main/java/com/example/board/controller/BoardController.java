package com.example.board.controller;

import com.example.board.model.Board;
import com.example.board.model.Comment;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.service.CommentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardRepository boardRepository;
    private final CommentService commentService;

    public BoardController(BoardRepository boardRepository, CommentRepository commentRepository, CommentService commentService) {
        this.boardRepository = boardRepository;
        this.commentService = commentService;
    }

    @GetMapping
    public String list(Model model, @RequestParam(value = "errorMessage", required = false) String errorMessage) {
        model.addAttribute("boards", boardRepository.findAll());
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage); // 에러 메시지 전달
        }
        return "board/list";
    }

    @PostMapping
    public String create(@ModelAttribute Board board) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        board.setAuthor(auth.getName());
        boardRepository.save(board);
        return "redirect:/board";
    }


    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Board board) {
        Board existingBoard = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 작성자 확인
        if (!existingBoard.getAuthor().equals(auth.getName())) {
            try {
                String errorMessage = URLEncoder.encode("작성자만 수정할 수 있습니다.", "UTF-8");
                return "redirect:/board?errorMessage=" + errorMessage;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        existingBoard.setTitle(board.getTitle());
        existingBoard.setContent(board.getContent());
        boardRepository.save(existingBoard);
        return "redirect:/board";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 작성자 확인
        if (!board.getAuthor().equals(auth.getName())) {
            try {
                String errorMessage = URLEncoder.encode("작성자만 삭제할 수 있습니다.", "UTF-8");
                return "redirect:/board?errorMessage=" + errorMessage;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        boardRepository.deleteById(id);
        return "redirect:/board";
    }

    @GetMapping("/detail/{id}")
    public String getBoardDetail(@PathVariable Long id, Model model) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: " + id));

        List<Comment> comments = commentService.getCommentHierarchy(board);

        model.addAttribute("board", board);
        model.addAttribute("comments", comments);
        return "board/detail";
    }


}
