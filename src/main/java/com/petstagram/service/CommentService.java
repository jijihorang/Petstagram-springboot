package com.petstagram.service;

import com.petstagram.dto.CommentDTO;
import com.petstagram.dto.ReplyCommentDTO;
import com.petstagram.dto.UserDTO;
import com.petstagram.entity.*;
import com.petstagram.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReplyCommentRepository replyCommentRepository;
    private final ReplyCommentLikeRepository replyCommentLikeRepository;
    private final NotificationRepository notificationRepository;

    private final NotificationService notificationService;

    // 댓글 작성
    public Long writeComment(Long postId, CommentDTO commentDTO) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용자를 찾을 수 없습니다."));

        CommentEntity commentEntity = CommentEntity.toEntity(commentDTO);
        commentEntity.setPost(post);
        commentEntity.setUser(currentUser);

        post.addComment(commentEntity);

        currentUser.addComment(commentEntity);

        CommentEntity savedEntity = commentRepository.save(commentEntity);

        // 댓글 알림 추가
        Long commentId = savedEntity.getId();
        Long postAuthorId = post.getUser().getId();
        notificationService.sendNotification(postAuthorId, "comment", currentUser.getId(), postId, commentId);

        return savedEntity.getId();
    }

    // 댓글 리스트 및 좋아요 개수 조회
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentList(Long postId) {
        List<CommentEntity> commentList = commentRepository.findByPostId(postId);
        return commentList.stream().map(commentEntity -> {
            CommentDTO commentDTO = CommentDTO.toDTO(commentEntity);

            // 특정 게시물에 대한 좋아요 개수 조회
            long likesCount = commentLikeRepository.countByComment(commentEntity);
            commentDTO.setCommentLikesCount(likesCount);

            return commentDTO;
        }).collect(Collectors.toList());
    }

    // 댓글 수정
    public CommentDTO updateComment(Long commentId, CommentDTO commentDTO) {
        // 댓글 찾기
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 현재 인증된 사용자의 이름(또는 이메일 등의 식별 정보) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!commentEntity.getUser().getEmail().equals(username)) {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        // 찾은 댓글의 내용을 업데이트
        commentEntity.setCommentContent(commentDTO.getCommentContent());

        // 댓글 저장
        commentRepository.save(commentEntity);

        return CommentDTO.toDTO(commentEntity);
    }

    public void deleteComment(Long commentId) {
        // 현재 인증된 사용자의 이름(또는 이메일 등의 식별 정보) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 댓글 ID로 댓글 찾기
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        // 댓글이 속한 게시글의 작성자 확인
        UserEntity postAuthor = commentEntity.getPost().getUser();

        // 댓글 소유자가 현재 인증된 사용자이거나 댓글이 속한 게시글의 작성자가 현재 인증된 사용자일 경우에만 삭제 가능
        if (!commentEntity.getUser().getEmail().equals(username) && !postAuthor.getEmail().equals(username)) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }

        // 댓글과 관련된 모든 알림을 삭제
        notificationRepository.deleteByCommentId(commentId);

        // 댓글에 달린 모든 대댓글을 삭제
        List<ReplyCommentEntity> replyComments = commentEntity.getReplyCommentList();
        for (ReplyCommentEntity replyComment : replyComments) {
            // 대댓글의 좋아요를 먼저 삭제
            replyCommentLikeRepository.deleteByReplyComment(replyComment);
        }
        replyCommentRepository.deleteAll(replyComments);

        // 댓글 삭제
        commentRepository.delete(commentEntity);
    }


    // 댓글 좋아요 추가 또는 삭제
    @Transactional
    public void toggleCommentLike(Long commentId) {

        // 댓글 찾기
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 현재 인증된 사용자의 이름(또는 이메일 등) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 좋아요가 이미 있는지 확인
        Optional<CommentLikeEntity> commentLikeOpt = commentLikeRepository.findByCommentAndUser(comment, user);

        if (commentLikeOpt.isPresent()) {
            // 좋아요 엔티티가 존재한다면, 상태를 false 로 설정하고 타임스탬프 업데이트
            CommentLikeEntity commentLikeEntity = commentLikeOpt.get();
            commentLikeEntity.setCommentStatus(!commentLikeEntity.isCommentStatus());
            commentLikeRepository.delete(commentLikeEntity);
        } else {
            // 좋아요가 없다면 추가
            CommentLikeEntity commentLikeEntity = new CommentLikeEntity();
            commentLikeEntity.setComment(comment);
            commentLikeEntity.setUser(user);
            commentLikeEntity.setCommentStatus(true);
            commentLikeRepository.save(commentLikeEntity);
        }
    }

    // 댓글 좋아요 상태 조회
    public CommentDTO getCommentLikeStatus(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 해당 댓글에 대한 사용자의 좋아요 여부 확인
        boolean isLiked = commentLikeRepository.findByCommentAndUser(comment, user).isPresent();

        // 해당 댓글의 총 좋아요 수 계산
        long likeCount = commentLikeRepository.countByComment(comment);

        // CommentDTO 객체 생성 및 반환
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCommentLiked(isLiked);
        commentDTO.setCommentLikesCount(likeCount);
        return commentDTO;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getCommentLikedUsers(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        List<CommentLikeEntity> likes = commentLikeRepository.findByComment(comment);

        return likes.stream()
                .map(like -> UserDTO.toDTO(like.getUser()))
                .collect(Collectors.toList());
    }

    // 대댓글 작성
    public Long writeReplyComment(Long commentId, ReplyCommentDTO replyCommentDTO) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용자를 찾을 수 없습니다."));

        if (replyCommentDTO.getReplyCommentContent() == null || replyCommentDTO.getReplyCommentContent().isEmpty()) {
            throw new IllegalArgumentException("대댓글 내용은 비어 있을 수 없습니다.");
        }

        ReplyCommentEntity replyCommentEntity = ReplyCommentEntity.builder()
                .replyCommentContent(replyCommentDTO.getReplyCommentContent())
                .comment(comment)
                .user(currentUser)
                .build();

        comment.addReplyComment(replyCommentEntity);

        ReplyCommentEntity savedEntity = replyCommentRepository.save(replyCommentEntity);

        return savedEntity.getId();
    }

    // 대댓글 조회
    @Transactional(readOnly = true)
    public List<ReplyCommentDTO> getReplyCommentList(Long commentId) {
        List<ReplyCommentEntity> replyCommentList = replyCommentRepository.findByCommentId(commentId);
        return replyCommentList.stream().map(ReplyCommentDTO::toDTO).collect(Collectors.toList());
    }

    // 대댓글 삭제
    public void deleteReplyComment(Long replyCommentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        ReplyCommentEntity replyCommentEntity = replyCommentRepository.findById(replyCommentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대댓글을 찾을 수 없습니다."));

        Long postAuthorId = replyCommentEntity.getComment().getPost().getUser().getId();
        Long replyCommentAuthorId = replyCommentEntity.getUser().getId();
        Long currentUserId = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다.")).getId();

        // 로그인한 사용자가 게시물 작성자인 경우 모든 대댓글 삭제 가능
        // 또는 대댓글 작성자인 경우 자신의 대댓글 삭제 가능
        if (postAuthorId.equals(currentUserId) || replyCommentAuthorId.equals(currentUserId)) {
            replyCommentLikeRepository.deleteByReplyComment(replyCommentEntity);
            replyCommentRepository.delete(replyCommentEntity);
        } else {
            throw new IllegalStateException("대댓글 삭제 권한이 없습니다.");
        }
    }

    // 대댓글 좋아요 추가 또는 삭제
    public void toggleReplyCommentLike(Long replyCommentId) {
        ReplyCommentEntity replyComment = replyCommentRepository.findById(replyCommentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대댓글이 존재하지 않습니다."));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Optional<ReplyCommentLikeEntity> replyCommentLikeOpt = replyCommentLikeRepository.findByReplyCommentAndUser(replyComment, user);

        if (replyCommentLikeOpt.isPresent()) {
            ReplyCommentLikeEntity replyCommentLikeEntity = replyCommentLikeOpt.get();
            replyCommentLikeEntity.setReplyCommentStatus(!replyCommentLikeEntity.isReplyCommentStatus());
            replyCommentLikeRepository.delete(replyCommentLikeEntity);
        } else {
            ReplyCommentLikeEntity replyCommentLikeEntity = new ReplyCommentLikeEntity();
            replyCommentLikeEntity.setReplyComment(replyComment);
            replyCommentLikeEntity.setUser(user);
            replyCommentLikeEntity.setReplyCommentStatus(true);
            replyCommentLikeRepository.save(replyCommentLikeEntity);
        }
    }

    // 대댓글 좋아요 상태 조회
    public ReplyCommentDTO getReplyCommentLikeStatus(Long replyCommentId) {
        ReplyCommentEntity replyComment = replyCommentRepository.findById(replyCommentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대댓글이 존재하지 않습니다."));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean isLiked = replyCommentLikeRepository.findByReplyCommentAndUser(replyComment, user).isPresent();
        long likeCount = replyCommentLikeRepository.countByReplyComment(replyComment);

        ReplyCommentDTO replyCommentDTO = ReplyCommentDTO.toDTO(replyComment);
        replyCommentDTO.setReplyCommentLiked(isLiked);
        replyCommentDTO.setReplyCommentLikesCount(likeCount);
        return replyCommentDTO;
    }

    // 대댓글 좋아요 누른 사용자 리스트 조회
    @Transactional(readOnly = true)
    public List<UserDTO> getReplyCommentLikedUsers(Long replyCommentId) {
        ReplyCommentEntity replyComment = replyCommentRepository.findById(replyCommentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대댓글이 존재하지 않습니다."));

        List<ReplyCommentLikeEntity> likes = replyCommentLikeRepository.findByReplyComment(replyComment);

        return likes.stream()
                .map(like -> UserDTO.toDTO(like.getUser()))
                .collect(Collectors.toList());
    }
}