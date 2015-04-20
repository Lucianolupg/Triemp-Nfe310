/**
 * @version 25/05/2010 
 * @author Triemp Solutions / Paulo Bueno <BR>
 * 
 * Projeto: Triemp-nfe <BR>
 * 
 * Pacote: br.com.triemp.modules.nfe <BR>
 * Classe: @(#)TriempNFEFactory.java <BR>
 * 
 * Este arquivo é parte do sistema Freedom-ERP, o Freedom-ERP é um software livre; você pode redistribui-lo e/ou <BR>
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como publicada pela Fundação do Software Livre (FSF); <BR>
 * na versão 2 da Licença, ou (na sua opnião) qualquer versão. <BR>
 * Este programa é distribuido na esperança que possa ser  util, mas SEM NENHUMA GARANTIA; <BR>
 * sem uma garantia implicita de ADEQUAÇÂO a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. <BR>
 * Veja a Licença Pública Geral GNU para maiores detalhes. <BR>
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU junto com este programa, se não, <BR>
 * de acordo com os termos da LPG-PC <BR>
 * <BR>
 */
package br.com.triemp.modules.nfe200.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.freedom.infra.functions.SystemFunctions;
import org.freedom.infra.model.jdbc.DbConnection;
import org.freedom.library.functions.Funcoes;
import org.freedom.modules.nfe.bean.AbstractNFEKey;
import org.freedom.modules.nfe.bean.FreedomNFEKey;

import br.com.triemp.nfe.util.NFeUtil;
import br.inf.portalfiscal.nfe.ObjectFactory;
import br.inf.portalfiscal.nfe.TEnderEmi;
import br.inf.portalfiscal.nfe.TEndereco;
import br.inf.portalfiscal.nfe.TNFe;
import br.inf.portalfiscal.nfe.TUf;
import br.inf.portalfiscal.nfe.TUfEmi;

public abstract class NFe {

	protected TNFe nfe;
	protected TNFe.InfNFe infNFe;
	protected TNFe.InfNFe.Ide ide;
	protected TNFe.InfNFe.Emit emit;
	protected TEnderEmi endEmit;
	protected TNFe.InfNFe.Dest dest;
	protected TEndereco endDest;
	protected TNFe.InfNFe.Total total;
	protected TNFe.InfNFe.Transp transp;
	protected TNFe.InfNFe.InfAdic infAdic;
	protected AbstractNFEKey key = null;
	protected DbConnection conSys = null;
	protected DbConnection conNFE = null;
	protected String crt = "";
	protected boolean simples = false;
	protected double aliqSimples = 0;
	protected BigDecimal vlrIcmsSimples = new BigDecimal(0);
	protected String msgSimples = null;
	protected NFe triempNFe;
	protected ArrayList<String> emailsNfe = new ArrayList<String>();
	protected boolean valid = true;
	protected String chaveNfe;
	protected String pathAtual;
	protected String separador;
	protected String pathFreedom;
	protected File xmlNFe = new File("");
	
	protected abstract void setStatusNFe(String pathXML);
	
	public NFe(DbConnection conSys, DbConnection conNFE, AbstractNFEKey key) {
		this.conSys = conSys;
		this.conNFE = conNFE;
		this.key = key;
		if (SystemFunctions.getOS() == SystemFunctions.OS_LINUX) {
			this.separador = "/";
		} else if (SystemFunctions.getOS() == SystemFunctions.OS_WINDOWS) {
			this.separador = "\\";
		}
		this.pathFreedom = (String) key.get(FreedomNFEKey.DIRNFE);
		
		nfe = new ObjectFactory().createTNFe();
		infNFe = new ObjectFactory().createTNFeInfNFe();
		nfe.setInfNFe(infNFe);
		ide = new ObjectFactory().createTNFeInfNFeIde();
		infNFe.setIde(ide);
		emit = new ObjectFactory().createTNFeInfNFeEmit();
		infNFe.setEmit(emit);
		endEmit = new ObjectFactory().createTEnderEmi();
		emit.setEnderEmit(endEmit);
		dest = new ObjectFactory().createTNFeInfNFeDest();
		endDest = new ObjectFactory().createTEndereco();
		dest.setEnderDest(endDest);
		infNFe.setDest(dest);
		total = new ObjectFactory().createTNFeInfNFeTotal();
		infNFe.setTotal(total);
		
		carregaPreferenciasNFe();
	}
	
	public void gerarNFe(){
		try{
			JAXBContext jaxbContext = JAXBContext.newInstance("br.inf.portalfiscal.nfe");
			Marshaller marshaller = jaxbContext.createMarshaller();
			//marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, new String("UTF-8"));

			NFeUtil.criaDiretorio(pathFreedom + separador + "enviar");
			pathAtual = "enviar" + separador + infNFe.getId().trim().replace("NFe", "") + "-nfe.xml";
			xmlNFe = new File(pathFreedom + separador + pathAtual);
			FileOutputStream fos = new FileOutputStream(xmlNFe);
			
			marshaller.marshal(nfe, fos);
			fos.close();
			
			this.setStatusNFe(pathAtual);
			
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void carregaInfoNFe() {
		infNFe.setVersao("2.00");
		PreparedStatement ps;
		ResultSet rs;
		String sql = null;
		Integer codigo = null;
		try {
			if(ide != null){
				if(ide.getTpNF().equals("0")){		 // 0 - Entrada
					sql = "SELECT CHAVENFECOMPRA AS CHAVENFE FROM CPCOMPRA WHERE CODEMP=? AND CODFILIAL=? AND CODCOMPRA=?";
					codigo = (Integer) key.get(FreedomNFEKey.CODCOMPRA);
				}else if(ide.getTpNF().equals("1")){// 1 - Saida
					sql = "SELECT CHAVENFEVENDA AS CHAVENFE FROM VDVENDA WHERE CODEMP=? AND CODFILIAL=? AND CODVENDA=? AND TIPOVENDA='V'";
					codigo = (Integer) key.get(FreedomNFEKey.CODVENDA);
				}
				ps = conSys.prepareStatement(sql);
				ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
				ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
				ps.setInt(3, codigo);
				rs = ps.executeQuery();
				
				if (rs.next()) {
					chaveNfe = rs.getString("CHAVENFE");
				}
			}
			if(chaveNfe != null && chaveNfe.trim().length() > 0){
				infNFe.setId("NFe" + chaveNfe);
				ide.setCNF(getString(chaveNfe.substring(35, 43), 8, true));
				ide.setCDV(getInteger(chaveNfe.substring(chaveNfe.length()-1), 1, true));
			}else{
				Random random = new Random();
				SimpleDateFormat format = new SimpleDateFormat("yyMM");
				String[] dtEmi = ide.getDEmi().split("-");
				String aamm = format.format(new GregorianCalendar(Integer.parseInt(dtEmi[0]), Integer.parseInt(dtEmi[1])-1, Integer.parseInt(dtEmi[2])).getTime());
				String cnpj = NFeUtil.lpad(emit.getCNPJ(), "0", 14);
				String mod = NFeUtil.lpad(ide.getMod(), "0", 2);
				String serie = NFeUtil.lpad(ide.getSerie(), "0", 3);
				String nNf = NFeUtil.lpad(ide.getNNF(), "0", 9);
				String tpEmis = ide.getTpEmis();
				String cnf = NFeUtil.lpad(String.valueOf(random.nextInt(99999999)),"0", 8);
				String id = ide.getCUF() + aamm + cnpj + mod + serie + nNf + tpEmis + cnf;
				String cdv = NFeUtil.getDvChaveNFe(id);
				infNFe.setId("NFe" + id + cdv);
				ide.setCNF(getString(cnf, 8, true));
				ide.setCDV(getInteger(cdv, 1, true));
				chaveNfe = id + cdv;
			}
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao carregar informações da Nota Fiscal!\n" + err.getMessage(), true, conSys, err);
		} catch (Exception err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao carregar informações da Nota Fiscal!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}
	}
	
	protected String getFormaPagamento(int codPlanoPag) {
		String indPag = "2";
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT DIASPAG, PERCPAG FROM FNPARCPAG WHERE CODPLANOPAG=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, codPlanoPag);
			rs = ps.executeQuery();

			while (rs.next()) {
				if ((rs.getInt("DIASPAG") == 0) && (rs.getInt("PERCPAG") == 100)) {
					indPag = "0";
				} else if ((rs.getInt("DIASPAG") > 0) && (rs.getInt("PERCPAG") < 100)) {
					indPag = "1";
				}
			}
			
			conSys.commit();
			
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null, "Erro ao carregar forma de pagamento da venda!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}

		return indPag;
	}

	protected void carregaPreferenciasNFe() {
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT P1.FORMATODANFE, P1.AMBIENTENFE, P1.PROCEMINFE, P1.VERPROCNFE, P1.REGIMETRIBNFE, M.MENS "
					+ "FROM SGPREFERE1 P1 LEFT JOIN LFMENSAGEM M ON (M.CODMENS=P1.CODMENSICMSSIMPLES AND M.CODEMP=P1.CODEMPMS AND M.CODFILIAL=P1.CODFILIALMS AND P1.CREDICMSSIMPLES='S') "
					+ "WHERE P1.CODEMP=? AND P1.CODFILIAL=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			rs = ps.executeQuery();

			if (rs.next()) {
				ide.setTpImp(getInteger(rs.getString("FORMATODANFE"), 1, true));
				ide.setTpEmis(getInteger("1", 1, true));
				ide.setTpAmb(getInteger(rs.getString("AMBIENTENFE"), 1, true));
				ide.setFinNFe(getInteger("1", 1, true));
				ide.setProcEmi(getInteger(rs.getString("PROCEMINFE"), 1, true));
				ide.setVerProc(getString(rs.getString("VERPROCNFE"), 20, true));
				if(rs.getString("MENS") != null){
					this.msgSimples = getString(rs.getString("MENS"));
				}
				this.crt = rs.getString("REGIMETRIBNFE");
			}
			
			conSys.commit();

		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao carregar preferências da NF-e!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}
	}
	
	protected void setInfAdic(String mens){
		if(mens != null){
			if(infAdic == null){
				infAdic = new ObjectFactory().createTNFeInfNFeInfAdic();
				infNFe.setInfAdic(infAdic);
			}
			infAdic.setInfCpl((infAdic.getInfCpl() != null)? infAdic.getInfCpl() + " | " + mens : mens);
		}
	}
	
	protected void setInfAdFisco(String mens){
		if(mens != null){
			if(infAdic == null){
				infAdic = new ObjectFactory().createTNFeInfNFeInfAdic();
				infNFe.setInfAdic(infAdic);
			}
			infAdic.setInfAdFisco((infAdic.getInfAdFisco() != null)? infAdic.getInfAdFisco() + " | " + mens : mens);
		}
	}

	protected String getString(String string) {
		return getString(string, -1);
	}

	protected String getString(String string, int tamanho) {
		return getString(string, tamanho, false);
	}
	
	protected String getString(String string, int tamanho, boolean obrig) {
		return getString(string, tamanho, obrig, null);
	}

	protected String getString(String string, int tamanho, boolean obrig, String str) {
		if (string != null) {
			string = string.trim();
			if(str != null){
				for(int i=0; i < str.length(); i++){
					string = string.replace(String.valueOf(str.charAt(i)), "");
				}
			}
			if (tamanho > 0 && string.length() > tamanho) {
				string = string.substring(0, tamanho);
			}
		} else if (obrig == true) {
			string = "";
		}
		string = removeAcento(string);
		return string;
	}

	protected String getInteger(String valor, int tamanho) {
		return getInteger(valor, tamanho, false);
	}

	protected String getInteger(String valor, int tamanho, boolean obrig) {
		if (valor != null && valor.matches("^[0-9]*[.]{0,1}[0-9]*$")) {
			valor = getDouble(valor.trim(), tamanho, 0);
			//valor = String.valueOf(Integer.valueOf(valor.trim()).intValue());
		} else if (obrig == true) {
			valor = "0";
		}
		return valor;
	}

	protected String getDouble(String valor, int tamanho, int dec) {
		return getDouble(valor, tamanho, dec, false);
	}
	
	protected String getDouble(String valor, int tamanho, int dec, boolean obrig) {
		if (valor != null && valor.matches("^[0-9]*[.]{0,1}[0-9]*$")) {
			BigDecimal bigDecimal = new BigDecimal(valor);
			valor = String.valueOf(bigDecimal.setScale(dec, BigDecimal.ROUND_HALF_UP)).trim();
		} else if (obrig == true) {
			BigDecimal bigDecimal = new BigDecimal("0");
			valor = String.valueOf(bigDecimal.setScale(dec, BigDecimal.ROUND_HALF_UP)).trim();
		}
		return valor;
	}

	protected String getDouble(String valor, int tamanho, int dec, boolean obrig, boolean naoZero) {
		if (valor != null) {
			BigDecimal bigDecimal = new BigDecimal(valor);
			bigDecimal = bigDecimal.setScale(dec, BigDecimal.ROUND_HALF_UP);
			if(bigDecimal.doubleValue() == 0 && naoZero){
				valor = null;
			}else{
				valor = String.valueOf(bigDecimal).trim();
			}
		} else if (obrig) {
			BigDecimal bigDecimal = new BigDecimal("0");
			valor = String.valueOf(bigDecimal.setScale(dec, BigDecimal.ROUND_HALF_UP)).trim();
		}
		return valor;
	}

	protected String getDate(String data) {
		return getDate(data, false);
	}

	protected String getDate(String data, boolean obrig) {
		if (data == null && obrig == true) {
			data = Funcoes.dataAAAAMMDD(new Date()).trim();
		}
		return data;
	}

	protected TUf getTUf(String uf) {
		for (TUf tuf : TUf.values()) {
			if (tuf.name().equals(uf)) {
				return TUf.valueOf(uf);
			}
		}
		return null;
	}

	protected TUfEmi getTUfEmi(String uf) {
		for (TUfEmi tuf : TUfEmi.values()) {
			if (tuf.name().equals(uf)) {
				return TUfEmi.valueOf(uf);
			}
		}
		return null;
	}
	
	protected String removeAcento(String string) {
		if(string != null){
			string = string.replaceAll("[ÂÀÁÄÃ]","A");  
			string = string.replaceAll("[âãàáä]","a");  
			string = string.replaceAll("[ÊÈÉË]","E");  
			string = string.replaceAll("[êèéë]","e");  
			string = string.replaceAll("[ÎÍÌÏ]","I");  
			string = string.replaceAll("[îíìï]","i");  
			string = string.replaceAll("[ÔÕÒÓÖ]","O");  
			string = string.replaceAll("[ôõòóö]","o");  
			string = string.replaceAll("[ÛÙÚÜ]","U");  
			string = string.replaceAll("[ûúùü]","u");  
			string = string.replaceAll("Ç","C");  
			string = string.replaceAll("ç","c");   
			string = string.replaceAll("[ýÿ]","y");  
			string = string.replaceAll("Ý","Y");  
			string = string.replaceAll("ñ","n");  
			string = string.replaceAll("Ñ","N");  
			string = string.replaceAll("['<>\\|/]","");
		}
		return string;  
	}
	public TNFe getNfe() {
		return nfe;
	}
	public String getChaveNfe() {
		return chaveNfe;
	}
	public void setChaveNfe(String chaveNfe) {
		this.chaveNfe = chaveNfe;
	}
	public String getPathAtual() {
		return pathAtual;
	}
	public void setPathAtual(String pathAtual) {
		this.pathAtual = pathAtual;
		this.setStatusNFe(pathAtual);
	}
	public String getSeparador() {
		return separador;
	}
	public void setSeparador(String separador) {
		this.separador = separador;
	}
	public AbstractNFEKey getKey() {
		return key;
	}
	public File getXmlNFe() {
		return xmlNFe;
	}
	public void setXmlNFe(File file){
		this.xmlNFe = file;
	}
	public String getPathFreedom() {
		return pathFreedom;
	}
	public DbConnection getConSys() {
		return conSys;
	}
	public DbConnection getConNFE() {
		return conNFE;
	}
	public ArrayList<String> getEmailsNfe() {
		return emailsNfe;
	}
	public void setEmailsNfe(String email, String nome){
		if(email != null)
			emailsNfe.add("[ " + nome + " ] " + email);
	}
	
}
