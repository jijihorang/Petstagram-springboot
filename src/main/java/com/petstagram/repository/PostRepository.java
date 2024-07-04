
package com.petstagram.repository;

import com.petstagram.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findAllByOrderByIdDesc();
    List<PostEntity> findByUserId(Long userId);

    @Query("SELECT p FROM PostEntity p JOIN FETCH p.user ORDER BY p.id DESC")
    List<PostEntity> findAllWithUser();

    // UserEntity 와의 관계
    @Query("SELECT p FROM PostEntity p JOIN FETCH p.user WHERE p.id = :postId")
    Optional<PostEntity> findByIdWithUser(@Param("postId") Long postId);

    // PostLikeEntity 와의 관계
    @Query("SELECT p FROM PostEntity p LEFT JOIN FETCH p.postLikeList WHERE p.user.id = :userId")
    List<PostEntity> findByUserIdWithLikes(@Param("userId") Long userId);
}
