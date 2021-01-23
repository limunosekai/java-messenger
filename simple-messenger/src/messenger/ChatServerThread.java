package messenger;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ChatServerThread extends Thread {
	// 멤버필드
	private Socket socket; // 서버로부터 받을 소켓
	private BufferedReader br; // 클라이언트의 메세지 수신
	private PrintWriter pw; // 클라이언트에게 메세지 전송
	private ArrayList<ChatServerThread> list; // 스레드 리스트
	private String id; // 사용자 아이디
	private String address; // 사용자 아이피주소
	
	// 생성자 매개변수로 소켓을 받음
	public ChatServerThread(Socket s) {
		socket = s;
	}
	
	@Override
	public void run() {
		try {
			// 생성된 소켓의 IP주소
			address = socket.getInetAddress().getHostAddress();
			// EUC_KR 인코딩 통일(한글깨짐 이슈)
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(),"EUC_KR"));
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"EUC_KR"),true);		

			while(true) {
				// 클라이언트의 메시지 수신
				String getMsg = br.readLine();
				String[] msg = getMsg.split("::"); // 프로토콜과 메세지 구분
				// 종료 호출시 반복문 종료 후 finally 실행
				if(msg[0].equals(Protocol.ENTER)) { // 입장
					id = msg[1];
					getMsg = "** "+id+" 님이 입장했습니다. **";
					System.out.println(address+" 접속 id = "+id);
					broadcast(getMsg); // 전체 유저에게 메세지
				} else if(msg[0].equals(Protocol.SEND_MESSAGE)){ // 메세지
					broadcast("["+msg[1]+"] : "+msg[2]); // 전체 유저에게 메세지
					
				} else if(msg[0].equals(Protocol.EXIT)) { // 퇴장
					id = msg[1];
					System.out.println(address=" 로그아웃 id = "+id);
					break; // 퇴장시 반복문 종료
				}
			}
		} catch(Exception e) {
			list.remove(this);
			address = socket.getInetAddress().getHostAddress();
			System.out.println(address+"와의 접속이 끊김");
		} finally {	// 반복문 종료시 실행
			list.remove(this); // 리스트에서 제거
			broadcast("** "+id+" 님이 퇴장했습니다. **");
			address = socket.getInetAddress().getHostAddress();
			System.out.println(address+"("+id+")와 접속 끊김");
			// 자원 반납
			try{if(br!=null)br.close();}catch(Exception e) {}
			try{if(pw!=null)pw.close();}catch(Exception e) {}
			try {if(socket != null) socket.close();} catch(Exception e) {}
		}
	}
	
	// 전체 메세지
	private void broadcast(String msg) {
		for(ChatServerThread t : list) {
			t.pw.println(msg);
		}
	}
	
	// 리스트 세팅
	public void setList(ArrayList<ChatServerThread> list) {
		this.list = list;
	}
}
