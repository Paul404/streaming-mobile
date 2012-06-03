package streaming.android;
/* ------------------
   Server
   usage: java Server [RTSP listening port]
   ---------------------- */


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.widget.TextView;

public class Server implements Runnable{

	// Variaveis RTP:
	// ----------------
	DatagramSocket RTPsocket; // socket pra ser usado pra enviar e receber pacotes UDP
	DatagramPacket senddp; // pacote UDP contendo o frame do video

	InetAddress ClientIPAddr; // endereço IP do cliente
	int RTP_dest_port = 0; // porta de destino para pacotes RTP (obtido do cliente RTSP)

	// Variaveis do video:
	// ----------------
	int imagenb = 0; // numero da imagem enviada atualmente
	VideoStream video; // objeto VideoStream object used to access video frames
	static int MJPEG_TYPE = 26; // RTP payload type for MJPEG video
	static int FRAME_PERIOD = 100; // Frame period of the video to stream, in ms
	static int VIDEO_LENGTH = 500; // length of the video in frames

	Timer timer; // timer used to send the images at the video frame rate
	byte[] buf; // buffer used to store the images to send to the client

	// RTSP variables
	// ----------------
	// rtsp states
	final static int INIT = 0;
	final static int READY = 1;
	final static int PLAYING = 2;
	// rtsp message types
	final static int SETUP = 3;
	final static int PLAY = 4;
	final static int PAUSE = 5;
	final static int TEARDOWN = 6;

	static int state; // RTSP Server state == INIT or READY or PLAY
	Socket RTSPsocket; // socket used to send/receive RTSP messages
	// input and output stream filters
	static BufferedReader RTSPBufferedReader;
	static BufferedWriter RTSPBufferedWriter;
	static String VideoFileName; // video file requested from the client
	static int RTSP_ID = 123456; // ID of the RTSP session
	int RTSPSeqNb = 0; // Sequence number of RTSP messages within the session

	final static String CRLF = "\r\n";

	// --------------------------------
	// Constructor
	// --------------------------------
	public Server() {

		// init Timer
		//timer = new Timer(FRAME_PERIOD, this);
		//timer.setInitialDelay(0);
		//timer.setCoalesce(true);
		
		// Inicializa um temporizador
		

		// allocate memory for the sending buffer
		buf = new byte[15000];

		// Handler to close the main window
		//addWindowListener(new WindowAdapter() {
		//	public void windowClosing(WindowEvent e) {
		//		// stop the timer and exit
		//		timer.stop();
		//		System.exit(0);
		//	}
		//});

		// GUI:
		//label = new JLabel("Send frame #        ", JLabel.CENTER);
		//getContentPane().add(label, BorderLayout.CENTER);
	}

	// ------------------------
	// Handler for timer
	// ------------------------
	public void run() {

		// se o numero da imagem atual é menor que o tamanho do video
		if (imagenb < VIDEO_LENGTH) {
			// atualiza o numero da imagem atual
			imagenb++;

			try {
				// obtem o proximo frame do video, bem como seu tamanho
				int image_length = video.getnextframe(buf);

				// Builds an RTPpacket object containing the frame
				RTPpacket rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb,
						imagenb * FRAME_PERIOD, buf, image_length);

				// get to total length of the full rtp packet to send
				int packet_length = rtp_packet.getlength();

				// retrieve the packet bitstream and store it in an array of
				// bytes
				byte[] packet_bits = new byte[packet_length];
				rtp_packet.getpacket(packet_bits);

				// send the packet as a DatagramPacket over the UDP socket
				senddp = new DatagramPacket(packet_bits, packet_length,
						ClientIPAddr, RTP_dest_port);
				RTPsocket.send(senddp);

				// System.out.println("Send frame #"+imagenb);
				// print the header bitstream
				rtp_packet.printheader();

				// update GUI
				//label.setText("Send frame #" + imagenb);
				//TODO: alterar o label da view
				System.out.println("Send frame #" + imagenb);
			} 
			catch (Exception ex) {
				System.out.println("Excecao detectada: " + ex);
				System.exit(0);
			}
		}
		else {
			// para o temporizador quando chega no final do video
			Thread.currentThread().stop();
		}
	}

	// ------------------------------------
	// Parse RTSP Request
	// ------------------------------------
	private int parse_RTSP_request() {
		int request_type = -1;
		try {
			// parse request line and extract the request_type:
			String RequestLine = RTSPBufferedReader.readLine();
			// System.out.println("RTSP Server - Received from Client:");
			System.out.println(RequestLine);

			StringTokenizer tokens = new StringTokenizer(RequestLine);
			String request_type_string = tokens.nextToken();

			// convert to request_type structure:
			if ((new String(request_type_string)).compareTo("SETUP") == 0)
				request_type = SETUP;
			else if ((new String(request_type_string)).compareTo("PLAY") == 0)
				request_type = PLAY;
			else if ((new String(request_type_string)).compareTo("PAUSE") == 0)
				request_type = PAUSE;
			else if ((new String(request_type_string)).compareTo("TEARDOWN") == 0)
				request_type = TEARDOWN;

			if (request_type == SETUP) {
				// extract VideoFileName from RequestLine
				VideoFileName = tokens.nextToken();
			}

			// parse the SeqNumLine and extract CSeq field
			String SeqNumLine = RTSPBufferedReader.readLine();
			System.out.println(SeqNumLine);
			tokens = new StringTokenizer(SeqNumLine);
			tokens.nextToken();
			RTSPSeqNb = Integer.parseInt(tokens.nextToken());

			// get LastLine
			String LastLine = RTSPBufferedReader.readLine();
			System.out.println(LastLine);

			if (request_type == SETUP) {
				// extract RTP_dest_port from LastLine
				tokens = new StringTokenizer(LastLine);
				for (int i = 0; i < 3; i++)
					tokens.nextToken(); // skip unused stuff
				RTP_dest_port = Integer.parseInt(tokens.nextToken());
			}
			// else LastLine will be the SessionId line ... do not check for
			// now.
		} catch (Exception ex) {
			System.out.println("Exception caught: " + ex);
			System.exit(0);
		}
		return (request_type);
	}

	// ------------------------------------
	// Send RTSP Response
	// ------------------------------------
	private void send_RTSP_response() {
		try {
			RTSPBufferedWriter.write("RTSP/1.0 200 OK" + CRLF);
			RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);
			RTSPBufferedWriter.write("Session: " + RTSP_ID + CRLF);
			RTSPBufferedWriter.flush();
			// System.out.println("RTSP Server - Sent response to Client.");
		} catch (Exception ex) {
			System.out.println("Exception caught: " + ex);
			System.exit(0);
		}
	}
	
	
	public static void start(int porta, Activity ac) throws Exception {
		// cria um objeto servidor
		final Server theServer = new Server();

		// pega a porta definida pelo usuario
		int RTSPport = porta;

		// Inicializa uma conexao TCP com cliente usando sessao RTSP
		ServerSocket listenSocket = new ServerSocket(RTSPport);
		theServer.RTSPsocket = listenSocket.accept();
		listenSocket.close();
		
		TextView tlog = (TextView)ac.findViewById(R.id.texto_log);
		

		// Pega o endereço IP do cliente
		theServer.ClientIPAddr = theServer.RTSPsocket.getInetAddress();

		// Inicializa o estado do RTP
		state = INIT;
		
		tlog.append(theServer.RTSPsocket.getInetAddress().getHostAddress() + "\n");
		tlog.invalidate();
		//return;

		
		// Define streams de entrada e saida
		RTSPBufferedReader = new BufferedReader(new InputStreamReader(theServer.RTSPsocket.getInputStream()));
		RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theServer.RTSPsocket.getOutputStream()));

		// Aguarda por uma mensagem de SETUP do cliente
		int request_type;
		boolean done = false;
		while (!done) {
			request_type = theServer.parse_RTSP_request(); // bloqueado

			if (request_type == SETUP) {
				done = true;
				// atualiza o estado do RTSP
				state = READY;
				System.out.println("Novo estado RTSP: READY");

				// Envia uma resposta
				theServer.send_RTSP_response();

				// Inicializa um objeto de VideoStream
				try {
					theServer.video = new VideoStream(VideoFileName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Inicializa um socket RTP
				try {
					theServer.RTPsocket = new DatagramSocket();
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
		
		// laço para controlar requisições RTP
		//while (true) {
		new Thread(new Runnable() {
			
			private ScheduledFuture sf;
			
			@Override
			public void run() {
					// TODO Auto-generated method stub
				
				//(while) {
				//	
				//}
					
				int request_type;
			
			    // reconhece o tipo de requisição
				request_type = theServer.parse_RTSP_request(); // bloqueado
				
	
				if ((request_type == PLAY) && (state == READY)) {
					// envia uma resposta de volta
					theServer.send_RTSP_response();
					// inicia um temporizador
					
					sf = scheduler.scheduleAtFixedRate(theServer, 0, FRAME_PERIOD, TimeUnit.MILLISECONDS);
					
					// atualiza o estado do RTSP
					state = PLAYING;
					System.out.println("Novo estado RTSP: PLAYING");
				}
				else if ((request_type == PAUSE) && (state == PLAYING)) {
					// envia uma resposta de volta
					theServer.send_RTSP_response();
					// para o temporizador
					//theServer.timer.stop();
					try {
						//scheduler.awaitTermination(500, TimeUnit.MILLISECONDS);
					    //theServer.wait();
						sf.wait(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
					//scheduler.shutdown();
					// atualiza o estado do RTSP
					state = READY;
					System.out.println("Novo estado RTSP: READY");
				}
				else if (request_type == TEARDOWN) {
					// envia uma resposta de volta
					theServer.send_RTSP_response();
					// para o temporizador
					//theServer.timer.stop();
					//scheduler.shutdownNow();
					
					// fecha os sockets
					try {
						//theServer.wait();
						sf.cancel(false);
						theServer.RTSPsocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
					theServer.RTPsocket.close();
					
					//encerra a aplicação
					System.exit(0);
				}
			}
			
		}).run();
		
	}

}