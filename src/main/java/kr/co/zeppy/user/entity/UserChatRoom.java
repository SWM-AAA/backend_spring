package kr.co.zeppy.user.entity;

import jakarta.persistence.*;
import kr.co.zeppy.chat.entity.ChatRoom;
import kr.co.zeppy.global.entity.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USER_CHATROOM")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChatRoom extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_chatroom_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    private ReadStatus readStatus;
}
