package com.example.board.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board; // 해당 댓글이 속한 게시글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; // 부모 댓글 (대댓글 기능)

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>(); // 자식 댓글 목록

    @Column(nullable = false)
    private String content; // 댓글 내용

    @Column(nullable = false)
    private String author; // 작성자

    @Column(nullable = false)
    private LocalDateTime createdDate; // 생성일시

    @Transient // DB와 매핑되지 않음
    private int depth; // 계층 깊이

    // 기본 생성자
    public Comment() {
        this.createdDate = LocalDateTime.now();
    }

}
