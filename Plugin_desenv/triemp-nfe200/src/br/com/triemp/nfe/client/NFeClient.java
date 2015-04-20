package br.com.triemp.nfe.client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.freedom.library.functions.Funcoes;

public class NFeClient {
	private String host;
	private int port;
	private int timeout;
	private Socket socket;
	private PrintStream printStream;
	private NFeClientThread nfeClientThread;
	
	public NFeClient(String host, int port){
		this(host, port, 2);
	}
	
	public NFeClient(String host, int port, int timeout){
		this.host = host;
		this.port = port;
		this.timeout = timeout;
		try {
			this.socket = new Socket(this.host, this.port);
			this.printStream = new PrintStream(this.socket.getOutputStream());		
			nfeClientThread = new NFeClientThread(this.socket);
			new Thread(nfeClientThread).start();
			this.getRetornoOperacao(2);
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Não foi possivel conectar no host "+this.host, "Falha ao conectar", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Não foi possivel conectar no host "+this.host, "Falha ao conectar", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public String getStatusServico(){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.STATUSSERVICO");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String assinarNFe(String file){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.ASSINARNFE(\""+file+"\")");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String validarNFe(String file){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.VALIDARNFe(\""+file+"\")");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String enviarNFe(String file, String lote, boolean assina, boolean danfe){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.ENVIARNFE(\""+file+"\","+lote+","+(assina?"1":"0")+","+(danfe?"1":"0")+")");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String enviarEmail(String email, String file, boolean danfePdf){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.ENVIAREMAIL(\""+email+"\",\""+file+"\","+(danfePdf?"1":"0")+")");
		printStream.println(".");
		return this.getRetornoOperacao(5);
	}
	
	public String criarNFe(String file, boolean retornaXML){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.CRIARNFE(\""+file+"\","+(retornaXML?"1":"0")+")");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String criarEnviarNFe(String file, String lote, boolean danfe){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.CRIARENVIARNFE(\""+file+"\","+lote+","+(danfe?"1":"0")+")");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	private String getRetornoOperacao(int seg){
		String ret;
		Funcoes.espera(seg);
		do{
			ret = this.nfeClientThread.getStringBuffer().toString();
		}while (ret.equals(""));
		return ret.replaceAll("", "");
	}
	
	public String getRetorno(){
		return this.nfeClientThread.getStringBuffer().toString().replaceAll("", "");
	}
	
	public void close(){
		this.nfeClientThread.stop();
		try {
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected class NFeClientThread implements Runnable {
		private Scanner scanner;
		private StringBuffer stringBuffer;
		private boolean run;
		
		public NFeClientThread(Socket socket){
			try {
				scanner = new Scanner(socket.getInputStream(), "ISO-8859-1");
				this.stringBuffer = new StringBuffer();
				this.run = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void run() {
			while(scanner.hasNextLine() && this.run){
				stringBuffer.append(scanner.nextLine()+"\n");
			}
		}
		public void stop(){
			this.run = false;
		}
		public String getStringBuffer(){
			return this.stringBuffer.toString();
		}
		public void resetStringBuffer(){
			this.stringBuffer = new StringBuffer();
		}
	}
}