package kr.co.zeppy.user.entity;


import com.fasterxml.jackson.databind.ser.Serializers;
import jakarta.persistence.*;
import kr.co.zeppy.chat.entity.ChatRoom;
import kr.co.zeppy.global.entity.BaseModel;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Table(name = "USER_CHATROOM")
@AllArgsConstructor
public class UserChatRoom extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "userchatroom_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;
}
