package kr.co.zeppy.chat.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import kr.co.zeppy.global.entity.BaseModel;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.entity.UserChatMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@AllArgsConstructor
@Table(name = "CHATMESSAGE")
public class ChatMessage extends BaseModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatmessage_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    private String message;
}