package messenger;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class MainApp extends Frame implements ActionListener {
	// 멤버 필드
	private static final long serialVersionUID = 1L; // 시리얼 번호
	private Socket socket = null; // 서버와 연결할 소켓
	private PrintWriter pw; // 서버에게 메세지를 송신
	private BufferedReader br; // 서버의 메세지를 수신
	private String ip; // 사용자 ip 주소
	private String id; // 사용자 id
	private ChatClientThread thread; // 서버의 메세지를 수신할 스레드
	private CardLayout cl; // 카드 레이아웃
	private Button connectBtn; // 접속 버튼
	private TextField tf_ip; // 아이피 입력창
	private TextField tf_id; // 아이디 입력창
	private Button sendBtn; // 보내기 버튼
	private Button exitBtn; // 나가기 버튼
	private TextArea ta; // 채팅창
	private TextField tf_input; // 메세지 입력창
	
	// 생성자
	public MainApp() {
		super("로그인");
		// 카드 레이아웃
		cl = new CardLayout();
		setLayout(cl);
		
		// 접속 화면(root)
		Panel login = new Panel();
		login.setLayout(new BorderLayout());
		// 타이틀
		login.add("North", new Label("사내 메신저 프로토타입",Label.CENTER));
		// 로그인 화면
		// ip 입력
		Panel login_sub = new Panel(); 
		Panel pIp = new Panel();
		pIp.add(new Label("접속할 IP : "));
		tf_ip = new TextField("127.0.0.1",20);
		pIp.add(tf_ip);
		// 대화명 입력
		Panel pName = new Panel();
		pName.add(new Label("대화명 입력 : "));
		tf_id = new TextField("홍길동 사원",17);
		pName.add(tf_id);
		login_sub.add(pIp);
		login_sub.add(pName);
		login.add("Center",login_sub);
		// 접속 버튼
		connectBtn = new Button("접속 하기");
		login.add("South", connectBtn);
		connectBtn.addActionListener(this);
		
		// 채팅 화면
		Panel chat = new Panel();
		chat.setLayout(new BorderLayout());
		chat.add("North", new Label("사내 메신저 프로토타입",Label.CENTER));
		// 채팅창
		ta = new TextArea(10,35);
		chat.add("Center",ta);
		// 메세지 입력창,버튼
		Panel chat_sub = new Panel();
		tf_input = new TextField("",25);
		sendBtn = new Button("보내기");
		exitBtn = new Button("나가기");
		
		chat_sub.add(tf_input);
		chat_sub.add(sendBtn);
		chat_sub.add(exitBtn);
		// 이벤트 걸기
		sendBtn.addActionListener(this);
		exitBtn.addActionListener(this);
		tf_input.addActionListener(this);
		chat.add("South", chat_sub);
		
		add(login,"로그인");
		add(chat,"채팅창");
		
		cl.show(this, "로그인");
		
		setSize(400,300);
		setVisible(true);
		// 종료 이벤트
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				returnResource();
				System.exit(0);	
			}
		});
	}

	// 접속할 서버의 아이피와 포트번호로 소켓 생성 후 사용자 아이디 서버로 전송
	public void init() throws IOException {
		ip = tf_ip.getText();
		socket = new Socket(ip, 4999);
		// EUC_KR 인코딩 통일(한글깨짐 이슈)
		pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"EUC_KR"),true);
		br = new BufferedReader(new InputStreamReader(socket.getInputStream(),"EUC_KR"));
		// id 송신 후 입장 메세지 출력
		id = tf_id.getText();
		pw.println(Protocol.ENTER+"::"+id);
		// 스레드 생성후 시작
		thread = new ChatClientThread(br);
		Thread t = new Thread(thread);
		t.start();
		// 화면 전환
		cl.show(this, "채팅창");
		tf_input.requestFocus();
	}	
	// 버튼 이벤트
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			Object obj = e.getSource();
			// 접속 버튼 클릭시
			if(obj == connectBtn) {
				init();
			}
			// 메세지 입력후 엔터 혹은 보내기 버튼 클릭시
			if(obj == tf_input || obj == sendBtn) {
				// null값 전송 막기
				if(tf_input.getText().equals("") || tf_input.getText().length() == 0) return;
				String sendMsg = Protocol.SEND_MESSAGE+"::"+id+"::"+tf_input.getText();
				pw.println(sendMsg);
				
				tf_input.setText("");
				tf_input.requestFocus();
			}
			// 나가기 버튼 클릭시
			if(obj == exitBtn) {
				System.exit(0);
				returnResource();
			}
		} catch(Exception e1) {
			ta.setText(e1.getMessage()+"\n");
		}
	}
	// 내부 클레스로 서버의 메세지를 수신할 스레드 정의
	class ChatClientThread implements Runnable{
		private String getMsg;
		private BufferedReader br;
		// 생성자 매개변수로 BufferedReader를 받음
		public ChatClientThread(BufferedReader br) {
			this.br = br;
		}
		@Override
		public void run() {
			try {
				// 서버의 메세지를 수신
				while(true) {
					getMsg = br.readLine();
					ta.append(getMsg+"\n");
				}
			} catch(IOException e) {
				e.printStackTrace();
			} 
		}
	}
	
	// 자원 반납
	public void returnResource() {
		try {if(br!=null)br.close();}catch(Exception e) {}
		try {if(pw!=null)br.close();}catch(Exception e) {}
		try {if(socket!=null)socket.close();}catch(Exception e) {}
	}
	// Main
	public static void main(String[] args) {
		new MainApp();
	}
}
