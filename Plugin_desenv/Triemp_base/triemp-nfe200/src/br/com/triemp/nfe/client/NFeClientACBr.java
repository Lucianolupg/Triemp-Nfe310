package br.com.triemp.nfe.client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JOptionPane;

/**
 * 
 * @author paulo
 *
 * Classe responsavel por fazer a comunicação com o ACBrNFeMonitor
 */

public class NFeClientACBr {
	private String host;
	private int port;
	private Socket socket;
	private PrintStream printStream;
	private NFeClientThread nfeClientThread;
	private static String STATUS = "[STATUS]";
	private static String RETORNO = "[RETORNO]";
	private static String CONSULTA = "[CONSULTA]";
	private static String CANCELAMENTO = "[CANCELAMENTO]";
	private static long TIMEOUT_READ = 80000;
	private static long TIMEOUT = 4000;
	
	public NFeClientACBr(String host, int port){
		this.host = host;
		this.port = port;
	}
	
	public boolean conectar(){
		try {
			this.socket = new Socket(this.host, this.port);
			this.printStream = new PrintStream(this.socket.getOutputStream());		
			nfeClientThread = new NFeClientThread(this.socket);
			new Thread(nfeClientThread).start();
			this.getRetornoOperacao(0);
			return true;
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Não foi possível conectar no host "+this.host, "Falha ao conectar", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Não foi possível conectar no host "+this.host, "Falha ao conectar", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	
	public HashMap<String, String> getStatusServico(){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.STATUSSERVICO");
		printStream.println(".");
		return montaRetorno(STATUS);
	}
	
	public String assinarNFe(String file){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.ASSINARNFE(\""+file+"\")");
		printStream.println(".");
		return this.getRetornoOperacao(TIMEOUT);
	}
	
	public String validarNFe(String file){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.VALIDARNFE(\""+file+"\")");
		printStream.println(".");
		return this.getRetornoOperacao(TIMEOUT);
	}
	
	public HashMap<String, String> consultarNFe(String file){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.CONSULTARNFE(\""+file+"\")");
		printStream.println(".");
		return montaRetorno(CONSULTA);
	}
	
	public HashMap<String, String> enviarNFe(String file, String lote, boolean assina, boolean danfe){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.ENVIARNFE(\""+file+"\","+lote+","+(assina?"1":"0")+","+(danfe?"1":"0")+")");
		printStream.println(".");
		return montaRetorno(RETORNO);
	}

	public String enviarEmail(String email, String file, boolean danfePdf){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.ENVIAREMAIL(\""+email+"\",\""+file+"\","+(danfePdf?"1":"0")+")");
		printStream.println(".");
		return this.getRetornoOperacao(TIMEOUT);
	}
	
	public String criarNFe(String file, boolean retornaXML){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.CRIARNFE(\""+file+"\","+(retornaXML?"1":"0")+")");
		printStream.println(".");
		return this.getRetornoOperacao(TIMEOUT);
	}
	
	public String criarEnviarNFe(String file, String lote, boolean danfe){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.CRIARENVIARNFE(\""+file+"\","+lote+","+(danfe?"1":"0")+")");
		printStream.println(".");
		return this.getRetornoOperacao(TIMEOUT);
	}
	
	public String imprimirDanfe(String file){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.IMPRIMIRDANFE(\""+file+"\")");
		printStream.println(".");
		return this.getRetornoOperacao(TIMEOUT);
	}
	
	public HashMap<String, String> cancelarNFe(String chave, String just){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.CANCELARNFE(\""+chave+"\","+just+")");
		printStream.println(".");
		return montaRetorno(CANCELAMENTO);
	}
	
	public HashMap<String, String> montaRetorno(String inicio){
		StringBuffer retStr = new StringBuffer(this.getRetornoOperacao(TIMEOUT));
		String[] ret = null;
		while((retStr.indexOf("OK:") == -1) && (retStr.indexOf("ERRO:") == -1)){
			retStr.append(this.getRetornoOperacao(TIMEOUT));
		}
		HashMap<String, String> retorno = new HashMap<String, String>();
		if(retStr.indexOf("OK:") != -1){
			long tempo = System.currentTimeMillis();
			long tempoAtual = 0;
			while(retStr.indexOf(inicio) == -1 && (tempoAtual - tempo ) < TIMEOUT_READ){
				retStr.append(this.getRetornoOperacao(TIMEOUT));
				tempoAtual = System.currentTimeMillis();
			}
			ret = retStr.substring(retStr.indexOf(inicio)).replaceAll("\r", "").split("\n");
			
			for(int i=1; i < ret.length;i++){
				String[] arr = ret[i].split("=");
				if(arr.length > 1){
					retorno.put(arr[0], arr[1]);
				}
			}
			
		}else if(retStr.indexOf("ERRO:") != -1){
			retorno.put("ERRO", retStr.toString().replace("ERRO:", ""));
		}
		
		return retorno;
	}

	private String getRetornoOperacao(long millis){
		long tempo = 0;
		long tempoAtual = 0;
		String ret = "";
		try {
			tempo = System.currentTimeMillis();
			Thread.sleep(millis);
			do{
				ret = this.nfeClientThread.getStringBuffer().toString().trim();
				tempoAtual = System.currentTimeMillis();
			}while (ret.equals("") && (tempoAtual - tempo ) < TIMEOUT_READ );
			if(ret.length() == 0){
				ret = "ERRO:Tempo máximo de espera excedido";
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return ret.replaceAll("", "");
	}
	
	public String getRetorno(){
		return this.nfeClientThread.getStringBuffer().toString().replaceAll("", "");
	}
	
	public boolean isClose(){
		return this.nfeClientThread.isStop();
	}
	
	public void close(){
		if(this.nfeClientThread != null){
			this.nfeClientThread.stop();
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected class NFeClientThread implements Runnable {
		private Scanner scanner;
		private StringBuffer stringBuffer;
		private boolean run = false;
		
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
		public boolean isStop(){
			if(this.run == true){
				return false;
			}else{
				return true;
			}
		}
		public String getStringBuffer(){
			return this.stringBuffer.toString();
		}
		public void resetStringBuffer(){
			this.stringBuffer = new StringBuffer();
		}
	}
}