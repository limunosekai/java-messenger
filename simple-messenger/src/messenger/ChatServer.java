package messenger;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
/**
 *	메인 서버 
 */
public class ChatServer {
	// 멤버필드
	private ServerSocket ss = null; // 서버 소켓
	private Socket socket = null; // 클라이언트와 통신할 소켓
	private ArrayList<ChatServerThread> list = new ArrayList<>(); //스레드 저장 리스트
	
	// 생성자
	public ChatServer() {
		try {
			// 서버 소켓 생성
			ss = new ServerSocket(4999);
			System.out.println("서버 대기중");
			// 무한루프를 돌며 상시 대기중
			while(true) {
				// 클라이언트 접속 요청 대기
				socket = ss.accept();
				InetAddress ip = socket.getInetAddress();
				String name = ip.getHostName();
				System.out.println(name+" 접속"); // 호스트명 출력
				// 스레드 객체 생성, 생성된 소켓을 넘겨줌
				ChatServerThread thread = new ChatServerThread(socket);
				list.add(thread);
				thread.setList(list); // 스레드 리스트 세팅
				thread.start();
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0); // 강제 종료
		}
	}
	public static void main(String[] args) {
		new ChatServer();
	}
}
