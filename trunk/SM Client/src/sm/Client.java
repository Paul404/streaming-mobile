package sm;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;

public class Client {

	
	
	// Variaveis RTP:
	// ----------------
	DatagramPacket rcvdp; // pacote UDP recebido do servidor
	DatagramSocket RTPsocket; // socket usado para enviar e receber pacotes UDP
	static int RTP_RCV_PORT = 25000; // porta onde o cliente vai receber os pacotes RTP

	Timer timer; // temporizador usado para receber dados do socket UDP
	byte[] buf; // buffer usado para armazenar dados recebidos do servidor

	
	
	// Variaveis RTSP:
	// ----------------
	
	// estados rtsp
	public final static int INIT = 0;
	final static int READY = 1;
	final static int PLAYING = 2;
	public static int state; // estado RTSP == INIT ou READY ou PLAYING
	public Socket RTSPsocket; // socket usado para enviar/receber mensagens RTSP
	
	// filtros de fluxo de entrada e saida
	public static BufferedReader RTSPBufferedReader;
	public static BufferedWriter RTSPBufferedWriter;
	public static String VideoFileName; // arquivo de video para solicitar ao servidor
	int RTSPSeqNb = 0; // numero de sequencia mensagens RTSP dentro da sessao
	int RTSPid = 0; // ID da sessao RTSP (obtida do servidor RTSP)

	final static String CRLF = "\r\n";

	// constantes de video:
	// ------------------
	static int MJPEG_TYPE = 26; // RTP payload type for MJPEG video

	
	
	// --------------------------
	// Construtor
	// --------------------------
	public Client() {

		// inicializa o  timporizador
		// --------------------------
		timer = new Timer(20, new timerListener());
		timer.setInitialDelay(0);
		timer.setCoalesce(true);

		// alocar memória suficiente para o buffer usado para receber dados do servidor
		buf = new byte[15000];
	}

	

	// ------------------------------------
	// Handler for buttons
	// ------------------------------------

	// .............
	// TO COMPLETE
	// .............

	// Handler for Setup button
	// -----------------------

	public void setupButton() {

		// System.out.println("Setup Button pressed !");

		if (state == INIT) {
			try {
				RTPsocket = new DatagramSocket(RTP_RCV_PORT);
				RTPsocket.setSoTimeout(5); // 5 milissegundos

				// construct a new DatagramSocket to receive RTP packets
				// from the server, on port RTP_RCV_PORT
				// RTPsocket = ...

				// set TimeOut value of the socket to 5msec.
				// ....

			} catch (SocketException se) {
				System.out.println("Socket exception: " + se);
				System.exit(0);
			}

			// init RTSP sequence number
			RTSPSeqNb = 1;

			// Send SETUP message to the server
			send_RTSP_request("SETUP");

			// Wait for the response
			if (parse_server_response() != 200)
				System.out.println("Invalid Server Response");
			else {
				// change RTSP state and print new state
				// state = ....
				// System.out.println("New RTSP state: ....");
				state = READY;
				System.out.println("Novo estado RTSP: READY");
			}
		}// else if state != INIT then do nothing
	}

	// Handler for Play button
	// -----------------------
	class playButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			// System.out.println("Play Button pressed !");

			if (state == READY) {
				// increase RTSP sequence number
				// .....
				RTSPSeqNb++;

				// Send PLAY message to the server
				send_RTSP_request("PLAY");

				// Wait for the response
				if (parse_server_response() != 200)
					System.out.println("Invalid Server Response");
				else {
					// change RTSP state and print out new state
					// .....
					// System.out.println("New RTSP state: ...")
					state = PLAYING;
					System.out.println("Novo estado RTP: PLAYING");

					// start the timer
					timer.start();
				}
			}// else if state != READY then do nothing
		}
	}

	// Handler for Pause button
	// -----------------------
	class pauseButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			// System.out.println("Pause Button pressed !");

			if (state == PLAYING) {
				// increase RTSP sequence number
				// ........
				RTSPSeqNb++;

				// Send PAUSE message to the server
				send_RTSP_request("PAUSE");

				// Wait for the response
				if (parse_server_response() != 200)
					System.out.println("Invalid Server Response");
				else {
					// change RTSP state and print out new state
					// ........
					// System.out.println("New RTSP state: ...");
					state = READY;

					System.out.println("Novo estado RTSP: Ready");

					// stop the timer
					timer.stop();
				}
			}
			// else if state != PLAYING then do nothing
		}
	}

	// Handler for Teardown button
	// -----------------------
	class tearButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			// System.out.println("Teardown Button pressed !");

			// increase RTSP sequence number
			// ..........
			RTSPSeqNb++;

			// Send TEARDOWN message to the server
			send_RTSP_request("TEARDOWN");

			// Wait for the response
			if (parse_server_response() != 200)
				System.out.println("Invalid Server Response");
			else {
				// change RTSP state and print out new state
				// ........
				// System.out.println("New RTSP state: ...");
				state = INIT;

				System.out.println("Novo estado RTSP: INIT");

				// stop the timer
				timer.stop();

				// exit
				System.exit(0);
			}
		}
	}

	// ------------------------------------
	// Handler for timer
	// ------------------------------------

	class timerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			// Construct a DatagramPacket to receive data from the UDP socket
			rcvdp = new DatagramPacket(buf, buf.length);

			try {
				// receive the DP from the socket:
				RTPsocket.receive(rcvdp);

				// create an RTPpacket object from the DP
				RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(),
						rcvdp.getLength());

				// print important header fields of the RTP packet received:
				System.out.println("Got RTP packet with SeqNum # "
						+ rtp_packet.getsequencenumber() + " TimeStamp "
						+ rtp_packet.gettimestamp() + " ms, of type "
						+ rtp_packet.getpayloadtype());

				// print header bitstream:
				rtp_packet.printheader();

				// get the payload bitstream from the RTPpacket object
				int payload_length = rtp_packet.getpayload_length();
				byte[] payload = new byte[payload_length];
				rtp_packet.getpayload(payload);

				// get an Image object from the payload bitstream
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Image image = toolkit.createImage(payload, 0, payload_length);

				// display the image as an ImageIcon object
				icon = new ImageIcon(image);
				iconLabel.setIcon(icon);
			} catch (InterruptedIOException iioe) {
				// System.out.println("Nothing to read");
			} catch (IOException ioe) {
				System.out.println("Exception caught: " + ioe);
			}
		}
	}

	// ------------------------------------
	// Parse Server Response
	// ------------------------------------
	private int parse_server_response() {
		int reply_code = 0;

		try {
			// parse status line and extract the reply_code:
			String StatusLine = RTSPBufferedReader.readLine();
			// System.out.println("RTSP Client - Received from Server:");
			System.out.println(StatusLine);

			StringTokenizer tokens = new StringTokenizer(StatusLine);
			tokens.nextToken(); // skip over the RTSP version
			reply_code = Integer.parseInt(tokens.nextToken());

			// if reply code is OK get and print the 2 other lines
			if (reply_code == 200) {
				String SeqNumLine = RTSPBufferedReader.readLine();
				System.out.println(SeqNumLine);

				String SessionLine = RTSPBufferedReader.readLine();
				System.out.println(SessionLine);

				// if state == INIT gets the Session Id from the SessionLine
				tokens = new StringTokenizer(SessionLine);
				tokens.nextToken(); // skip over the Session:
				RTSPid = Integer.parseInt(tokens.nextToken());
			}
		} catch (Exception ex) {
			System.out.println("Exception caught: " + ex);
			System.exit(0);
		}

		return (reply_code);
	}

	// ------------------------------------
	// Send RTSP Request
	// ------------------------------------

	// .............
	// TO COMPLETE
	// .............

	private void send_RTSP_request(String request_type) {
		try {
			// Use the RTSPBufferedWriter to write to the RTSP socket

			// write the request line:
			RTSPBufferedWriter.write(request_type + " " + VideoFileName
					+ " RTSP/1.0" + CRLF);

			// write the CSeq line:
			// ......
			RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);

			// check if request_type is equal to "SETUP" and in this case write
			// the Transport: line advertising to the server the port used to
			// receive the RTP packets RTP_RCV_PORT
			// if ....
			// otherwise, write the Session line from the RTSPid field
			// else ....
			if (request_type.equals("SETUP")) {
				RTSPBufferedWriter.write("Transport: RTP/UDP; client_port= "
						+ RTP_RCV_PORT + CRLF);
			} else {
				RTSPBufferedWriter.write("Session: " + RTSPid + CRLF);
			}

			RTSPBufferedWriter.flush();

			// RTSPSeqNb++;
		} catch (Exception ex) {
			System.out.println("Exception caught: " + ex);
			System.exit(0);
		}
	}

}// end of Class Client
