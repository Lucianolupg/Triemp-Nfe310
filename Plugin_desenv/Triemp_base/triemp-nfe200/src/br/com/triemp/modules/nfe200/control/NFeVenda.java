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
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;

import org.freedom.infra.functions.SystemFunctions;
import org.freedom.infra.model.jdbc.DbConnection;
import org.freedom.library.functions.Funcoes;
import org.freedom.modules.nfe.bean.AbstractNFEKey;
import org.freedom.modules.nfe.bean.FreedomNFEKey;

import br.com.triemp.nfe.util.NFeUtil;
import br.inf.portalfiscal.nfe.ObjectFactory;
import br.inf.portalfiscal.nfe.TNFe;
import br.inf.portalfiscal.nfe.TUf;
import br.inf.portalfiscal.nfe.TVeiculo;

public class NFeVenda extends NFe {
	
	public NFeVenda(DbConnection conSys, DbConnection conNFE, AbstractNFEKey key) {
		super(conSys, conNFE, key);
		carregaVenda();
		carregaInfoNFe();
		carregaItVenda();
		carregaInfTransporte();
		carregaCobranca();
		carregaXmlNFe();
	}
	
	public boolean carregaXmlNFe(){
		String chave = "";
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT COALESCE(CHAVENFEVENDA,'') AS CHAVENFE FROM VDVENDA WHERE CODEMP=? AND CODFILIAL=? AND CODVENDA=? AND TIPOVENDA='V'";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODVENDA));
			rs = ps.executeQuery();

			if (rs.next()) {
				chave = rs.getString("CHAVENFE").trim();
			}
			conSys.commit();
			
			if(chave.length() > 0){
				sql = "SELECT COALESCE(PATHNFE,'') as PATHNFE FROM VDNFE WHERE CODEMP=? AND CODFILIAL=? AND CODVENDA=?"; // AND CHAVENFE=?";
				ps = conNFE.prepareStatement(sql);
				ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
				ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
				ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODVENDA));
				//ps.setString(4, chaveNfe);
				rs = ps.executeQuery();
				if(rs.next()){
					pathAtual = rs.getString("PATHNFE");
					if (SystemFunctions.getOS() == SystemFunctions.OS_LINUX) {
						pathAtual = pathAtual.replace("\\", "/");
					} else if (SystemFunctions.getOS() == SystemFunctions.OS_WINDOWS) {
						pathAtual = pathAtual.replace("/", "\\");
					}
					xmlNFe = new File(pathFreedom + separador + pathAtual);	
				}
				conNFE.commit();
			}
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao verificar arquivo da NFe!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}
		if(!xmlNFe.exists() || xmlNFe.isDirectory()){
			return false;
		}
		return true;
	}

	private boolean carregaVenda() {
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT fi.RAZFILIAL, fi.NOMEFILIAL, fi.CNPJFILIAL, fi.INSCFILIAL, fi.INSCMUNFILIAL, fi.CNAEFILIAL, fi.DDDFILIAL, fi.FONEFILIAL, fi.ENDFILIAL, fi.COMPLFILIAL, fi.NUMFILIAL, fi.BAIRFILIAL, fi.CEPFILIAL, fi.CODMUNIC, mu.NOMEMUNIC, uf.CODUF, uf.SIGLAUF, fi.CODPAIS, pa.NOMEPAIS, pa.CODBACENPAIS, fi.PERCPISFILIAL, fi.PERCCOFINSFILIAL, fi.PERCIRFILIAL, fi.PERCCSOCIALFILIAL, fi.SIMPLESFILIAL, fi.PERCSIMPLESFILIAL, "
			+ "vd.CHAVENFEVENDA, vd.OBSVENDA, vd.CODFILIAL, vd.CODPLANOPAG, vd.DTEMITVENDA, vd.DTSAIDAVENDA, vd.CODCLI, vd.SERIE, vd.DOCVENDA, vd.CALCISSVENDA, vd.VLRBASEICMSVENDA, vd.VLRICMSVENDA, vd.VLRBASEICMSSTVENDA, vd.VLRICMSSTVENDA, vd.VLRPRODVENDA, vd.VLRFRETEVENDA, vd.VLRDESCVENDA, vd.VLRIPIVENDA, vd.VLRPISVENDA, vd.VLRCOFINSVENDA, vd.VLRADICVENDA, vd.VLRLIQVENDA, vd.VLRBASEISSVENDA, vd.VLRISSVENDA, vd.VLRICMSSIMPLES, vd.PERCICMSSIMPLES, vdf.VLRSEGFRETEVD "
				+ "FROM VDVENDA vd INNER JOIN SGFILIAL fi ON (vd.CODFILIAL = fi.CODFILIAL) INNER JOIN SGUF uf ON (fi.SIGLAUF = uf.SIGLAUF) "
				+ "INNER JOIN SGMUNICIPIO mu ON (fi.CODMUNIC = mu.CODMUNIC AND uf.SIGLAUF = mu.SIGLAUF) INNER JOIN SGPAIS pa ON (fi.CODPAIS = pa.CODPAIS) "
				+ "LEFT JOIN VDFRETEVD vdf ON (vd.codemp=vdf.codemp and vd.codfilial=vdf.codfilial and vd.codvenda=vdf.codvenda and vd.tipovenda=vdf.tipovenda) "
				+ "WHERE vd.CODEMP=? AND vd.CODFILIAL=? AND vd.CODVENDA=? AND vd.TIPOVENDA='V'";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODVENDA));
			rs = ps.executeQuery();

			if (rs.next()) {
				if(getString(rs.getString("SIMPLESFILIAL")).equals("S")){
					this.simples = true;
					this.aliqSimples = rs.getDouble("PERCICMSSIMPLES");
				}

				ide.setCUF(getInteger(rs.getString("CODUF"), 2, true));
				ide.setIndPag(getInteger(getFormaPagamento(rs.getInt("CODPLANOPAG")), 1, true));
				ide.setMod(getString("55", 2, true));
				ide.setSerie(getString(rs.getString("SERIE"), 3, true));
				ide.setNNF(getInteger(rs.getString("DOCVENDA"), 9, true));
				ide.setDEmi(getDate(rs.getString("DTEMITVENDA"), true));
				ide.setDSaiEnt(getDate(rs.getString("DTSAIDAVENDA")));
				ide.setHSaiEnt(new SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
				ide.setTpNF(getString("1", 1, true));
				ide.setCMunFG(getString(rs.getString("CODUF"), 2, true) + getString(rs.getString("CODMUNIC"), 5, true));
				
				emit.setCNPJ(getString(rs.getString("CNPJFILIAL"), 14, true, "./-"));
				emit.setIE(getString(rs.getString("INSCFILIAL"), 14, true, "./-"));
				emit.setXNome(getString(rs.getString("RAZFILIAL"), 60, true));
				emit.setXFant(getString(rs.getString("NOMEFILIAL"), 60));
				emit.setIM(getString(rs.getString("INSCMUNFILIAL"), 15));
				emit.setCNAE(getString(rs.getString("CNAEFILIAL"), 7));
				emit.setCRT(getString(this.crt, 1));

				endEmit.setXLgr(getString(rs.getString("ENDFILIAL"), 60, true));
				endEmit.setNro(getString(rs.getString("NUMFILIAL"), 60, true));
				endEmit.setXBairro(getString(rs.getString("BAIRFILIAL"), 60, true));
				endEmit.setXCpl(getString(rs.getString("COMPLFILIAL"), 60));
				endEmit.setCEP(getString(rs.getString("CEPFILIAL"), 8, true, ".-"));
				endEmit.setCMun(getString(rs.getString("CODUF"), 2, true) + getString(rs.getString("CODMUNIC"), 5, true));
				endEmit.setXMun(getString(rs.getString("NOMEMUNIC"), 60, true));
				endEmit.setUF(getTUfEmi(rs.getString("SIGLAUF")));
				endEmit.setCPais(getString(rs.getString("CODBACENPAIS"), 4));
				endEmit.setXPais(getString(rs.getString("NOMEPAIS"), 60));
				if (rs.getString("FONEFILIAL") != null) {
					endEmit.setFone(getString(rs.getString("DDDFILIAL"), 2, true, "()") + getString(rs.getString("FONEFILIAL"), 14));
				}

				TNFe.InfNFe.Total.ICMSTot icmsTot = new ObjectFactory().createTNFeInfNFeTotalICMSTot();
				total.setICMSTot(icmsTot);
				
				icmsTot.setVBC(getDouble(rs.getString("VLRBASEICMSVENDA"), 15, 2, true));
				icmsTot.setVICMS(getDouble(rs.getString("VLRICMSVENDA"), 15, 2, true));
				icmsTot.setVBCST(getDouble(rs.getString("VLRBASEICMSSTVENDA"), 15, 2, true));
				icmsTot.setVST(getDouble(rs.getString("VLRICMSSTVENDA"), 15, 2, true));
				icmsTot.setVProd(getDouble(rs.getString("VLRPRODVENDA"), 15, 2, true));
				
				//Os valores abaixo são rateados nos itens
				icmsTot.setVDesc(getDouble(rs.getString("VLRDESCVENDA"), 15, 2));
				icmsTot.setVFrete(getDouble(rs.getString("VLRFRETEVENDA"), 15, 2));
				icmsTot.setVSeg(getDouble(rs.getString("VLRSEGFRETEVD"), 15, 2, true));
				icmsTot.setVOutro(getDouble(rs.getString("VLRADICVENDA"), 15, 2, true));
				//
				icmsTot.setVII(getDouble("0", 15, 2)); // Valor total do Imposto de Importação. Não implementado no Freedom-erp
				icmsTot.setVIPI(getDouble(rs.getString("VLRIPIVENDA"), 15, 2, true));
				icmsTot.setVPIS(getDouble(rs.getString("VLRPISVENDA"), 15, 2, true));
				icmsTot.setVCOFINS(getDouble(rs.getString("VLRCOFINSVENDA"), 15, 2, true));
				icmsTot.setVNF(getDouble(rs.getString("VLRLIQVENDA"), 15, 2, true));

				if(rs.getDouble("VLRBASEISSVENDA") > 0){
					TNFe.InfNFe.Total.ISSQNtot issqnTot = new ObjectFactory().createTNFeInfNFeTotalISSQNtot();
					total.setISSQNtot(issqnTot);
					
					issqnTot.setVServ(getDouble(rs.getString("VLRBASEISSVENDA"),15, 2)); // Verificar 343 | W18 - 4.01-NT2009.006x
					issqnTot.setVBC(getDouble(rs.getString("VLRBASEISSVENDA"), 15, 2));
					issqnTot.setVISS(getDouble(rs.getString("VLRISSVENDA"), 15, 2));
					issqnTot.setVPIS(getDouble(String.valueOf((rs.getDouble("VLRBASEISSVENDA") * rs.getDouble("PERCPISFILIAL") / 100)), 15, 2));
					issqnTot.setVCOFINS(getDouble(String.valueOf((rs.getDouble("VLRBASEISSVENDA") * rs.getDouble("PERCCOFINSFILIAL") / 100)), 15, 2));
				}
				
				this.setInfAdic(getString(rs.getString("OBSVENDA")));
				
				/*
				 * TODO - 348 | W23 - Grupo de Retenção de tributos - 4.01-NT2009.006x
				 * TNFe.InfNFe.Total.RetTrib retTrib = new ObjectFactory().createTNFeInfNFeTotalRetTrib();
				 * total.setRetTrib(retTrib); //retTrib.setVRetPIS();
				 * retTrib.setVRetCOFINS(); retTrib.setVRetCSLL();
				 * retTrib.setVBCIRRF(); retTrib.setVIRRF();
				 * retTrib.setVRetPrev();
				 */
				
				carregaDestinatarioNF(rs.getString("CODCLI"));
			}
			
			conSys.commit();
			
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao carregar informações da venda!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
			
		}
		return true;
	}

	private void carregaDestinatarioNF(String codCli) {
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT cli.RAZCLI, cli.PESSOACLI, cli.ATIVOCLI, cli.CNPJCLI, cli.INSCCLI, cli.CPFCLI, cli.SUFRAMACLI, cli.ENDCLI, cli.NUMCLI, cli.COMPLCLI, cli.BAIRCLI, cli.CIDCLI, cli.UFCLI, cli.CEPCLI, cli.DDDCLI, cli.FONECLI, cli.CODMUNIC, mu.NOMEMUNIC, uf.CODUF, cli.SIGLAUF, cli.CODPAIS, COALESCE(cli.EMAILNFECLI, cli.EMAILCLI) AS EMAIL, pa.NOMEPAIS, pa.CODBACENPAIS "
				+ "FROM VDCLIENTE cli INNER JOIN SGUF uf ON (cli.SIGLAUF = uf.SIGLAUF) "
				+ "INNER JOIN SGMUNICIPIO mu ON (cli.CODMUNIC = mu.CODMUNIC AND uf.SIGLAUF = mu.SIGLAUF) "
				+ "INNER JOIN SGPAIS pa ON (cli.CODPAIS = pa.CODPAIS) "
				+ "WHERE CODEMP=? AND CODFILIAL=? AND CODCLI=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, Integer.parseInt(codCli));
			rs = ps.executeQuery();
			if (rs.next()) {
				String cMun = getString(rs.getString("CODUF"), 2, true) + getString(rs.getString("CODMUNIC"), 5, true);
				dest.setXNome(getString(rs.getString("RAZCLI"), 60, true));
				if (rs.getString("PESSOACLI").trim().equals("J")) {
					dest.setCNPJ(getString(rs.getString("CNPJCLI"), 14, true, "./-"));
				} else if (rs.getString("PESSOACLI").trim().equals("F")) {
					dest.setCPF(getString(rs.getString("CPFCLI"), 11, true, "./-"));
				}
				if(rs.getString("INSCCLI") == null){
					dest.setIE(getString("ISENTO", 14, false, "./-"));
				}else{
					dest.setIE(getString(rs.getString("INSCCLI"), 14, false, "./-"));
				}
				dest.setISUF(getString(rs.getString("SUFRAMACLI"), 9));
				
				endDest.setXLgr(getString(rs.getString("ENDCLI"), 60, true));
				endDest.setNro(getString(rs.getString("NUMCLI"), 60, true));
				endDest.setXCpl(getString(rs.getString("COMPLCLI"), 60));
				endDest.setXBairro(getString(rs.getString("BAIRCLI"), 60, true));
				endDest.setCEP(getString(rs.getString("CEPCLI"), 8, false, "./-"));
				endDest.setCMun(cMun);
				endDest.setXMun(getString(rs.getString("NOMEMUNIC"), 60, true));
				endDest.setUF(getTUf(rs.getString("SIGLAUF")));
				endDest.setCPais(getString(rs.getString("CODBACENPAIS"), 4));
				endDest.setXPais(getString(rs.getString("NOMEPAIS"), 60));
				if (rs.getString("SUFRAMACLI") != null) {
					dest.setISUF(getString(rs.getString("SUFRAMACLI"), 9, false, "./-"));
				}
				if(rs.getString("EMAIL") != null){
					setEmailsNfe(getString(rs.getString("EMAIL")), "CLIENTE");
					dest.setEmail(getString(rs.getString("EMAIL")));
				}
				if (rs.getString("FONECLI") != null) {
					endDest.setFone(getString(rs.getString("DDDCLI"), 2, false, "()") + getString(rs.getString("FONECLI"), 14, false, "-"));
				}
			}
			conSys.commit();
			
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null, "Erro ao carregar informações do cliente!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}
	}

	private void carregaItVenda() {
		PreparedStatement ps;
		ResultSet rs;
		TNFe.InfNFe.Det det = new ObjectFactory().createTNFeInfNFeDet();
		TNFe.InfNFe.Det.Prod prod = new ObjectFactory().createTNFeInfNFeDetProd();
		
		//Variaveis iniciadas com os valores totais da tabela de venda
		BigDecimal vTotalDesconto = new BigDecimal(total.getICMSTot().getVDesc());
		BigDecimal vTotalFrete = new BigDecimal(total.getICMSTot().getVFrete());
		BigDecimal vTotalSeguro = new BigDecimal(total.getICMSTot().getVSeg());
		BigDecimal vTotalOutro = new BigDecimal(total.getICMSTot().getVOutro());
		BigDecimal vTotProd = new BigDecimal(total.getICMSTot().getVProd());
		
		//Variaveis para usadas para guardar os valores somados dos itens
		BigDecimal sTotalDesconto = new BigDecimal(0).setScale(2);
		BigDecimal sTotalFrete = new BigDecimal(0).setScale(2);
		BigDecimal sTotalSeguro = new BigDecimal(0).setScale(2);
		BigDecimal sTotalOutro = new BigDecimal(0).setScale(2);
		BigDecimal sTotalIcmsSt = new BigDecimal(0).setScale(2);
		BigDecimal sTotalIpi = new BigDecimal(0).setScale(2);
		
		String sql = "SELECT pro.CODBARPROD, pro.DESCPROD, pro.CODUNID, pro.TIPOPROD, natop.DESCNAT, clf.CODFISC, clf.CODNCM, clf.CODSERV, "
				+ "clfit.CODITFISC, clfit.ORIGFISC, clfit.MODBCICMS, clfit.MODBCICMSST,  clfit.REDFISC, clfit.REDBASEST, clfit.CODSITTRIBIPI, clfit.TPCALCIPI, clfit.VLRIPIUNIDTRIB, clfit.CODSITTRIBPIS, clfit.ALIQPISFISC, clfit.VLRPISUNIDTRIB, clfit.CODSITTRIBCOF, clfit.ALIQCOFINSFISC, clfit.VLRCOFUNIDTRIB, clfit.CSOSN, " 
				+ "it.CODEMP, it.CODFILIAL, it.TIPOVENDA, it.CODVENDA, it.CODITVENDA, it.CODNAT, it.CODPROD, it.CODLOTE, it.CODALMOX, it.QTDITVENDA, it.PRECOITVENDA, it.PERCDESCITVENDA, it.VLRDESCITVENDA, it.PERCICMSITVENDA, it.VLRBASEICMSITVENDA, it.VLRICMSITVENDA, it.PERCIPIITVENDA, it.VLRBASEIPIITVENDA, it.VLRIPIITVENDA, it.VLRLIQITVENDA, it.PERCCOMISITVENDA, it.VLRCOMISITVENDA, it.VLRADICITVENDA, it.PERCISSITVENDA, it.VLRISSITVENDA, it.VLRFRETEITVENDA, it.VLRPRODITVENDA, it.VLRISENTASITVENDA, it.VLROUTRASITVENDA, it.REFPROD, it.VLRBASEISSITVENDA, it.VLRBASEICMSBRUTITVENDA, it.VLRBASEICMSSTITVENDA, it.VLRICMSSTITVENDA, it.MARGEMVLAGRITVENDA, it.ORIGFISC, it.CODTRATTRIB, it.TIPOFISC, it.TIPOST " 
				+ "FROM VDITVENDA it INNER JOIN EQPRODUTO pro ON (pro.CODPROD = it.CODPROD and it.CODEMPPD = pro.CODEMP and it.CODFILIALPD = pro.CODFILIAL) LEFT JOIN LFNATOPER natop ON (natop.CODNAT = it.CODNAT and it.CODEMPNT = natop.CODEMP and it.CODFILIALNT = natop.CODFILIAL) LEFT JOIN LFCLFISCAL clf ON (clf.CODFISC = it.CODFISC and it.CODEMPIF = clf.CODEMP and it.CODFILIALIF = clf.CODFILIAL) LEFT JOIN LFITCLFISCAL clfit ON (clfit.codfisc = it.codfisc and clfit.CODITFISC = it.CODITFISC AND it.CODEMPIF = clfit.CODEMP and it.CODFILIALIF = clfit.CODFILIAL) "
				+ "WHERE it.CODEMP=? AND it.CODFILIAL=? AND it.CODVENDA=? AND it.TIPOVENDA='V' AND it.CANCITVENDA IS NULL ORDER BY it.CODITVENDA";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODVENDA));
			rs = ps.executeQuery();
			int nItem = 0;
			while (rs.next()) {
				if(ide.getNatOp() == null){
					//ide.setNatOp(getString(rs.getString("CODNAT"), 60, true)); // Código da CFOP
					ide.setNatOp(getString(rs.getString("DESCNAT"), 60, true)); // Descrição da CFOP
				}
				det = new ObjectFactory().createTNFeInfNFeDet();
				prod = new ObjectFactory().createTNFeInfNFeDetProd();
				det.setNItem(getInteger(String.valueOf(++nItem), 3, true));

//				prod.setCProd(getInteger(rs.getString("CODPROD"), 60, true));
				prod.setCProd(getString(rs.getString("REFPROD"), 60, true));
				
				String ean = getString(rs.getString("CODBARPROD"), 14, true);
				if(NFeUtil.isValidBarCodeEAN(ean)){ 
					prod.setCEAN(ean);
					prod.setCEANTrib(ean);
				}else{
					prod.setCEAN("");
					prod.setCEANTrib("");
				}
				prod.setXProd(getString(rs.getString("DESCPROD"), 120, true));
				prod.setCFOP(getString(rs.getString("CODNAT"), 4, true, "."));
				prod.setUCom(getString(rs.getString("CODUNID"), 6, true));
				prod.setQCom(getDouble(rs.getString("QTDITVENDA"), 11, 4, true));
				prod.setVUnCom(getDouble(rs.getString("PRECOITVENDA"), 16, 4, true));
				prod.setVProd(getDouble(rs.getString("VLRPRODITVENDA"), 15, 2, true));
				prod.setUTrib(getString(rs.getString("CODUNID"), 6, true));
				prod.setQTrib(getDouble(rs.getString("QTDITVENDA"), 11, 4, true));
				prod.setVUnTrib(getDouble(rs.getString("PRECOITVENDA"), 16, 4, true));
				
				/**
				 * Calculos de rateio proporcional de valores nos itens
				 */
				if(vTotProd.doubleValue() > 0){
					BigDecimal percItem = new BigDecimal(0).setScale(2);
					Double vProd = new Double(getDouble(prod.getVProd(), 15, 2, true));
					
					if(vProd > 0){
						percItem = new BigDecimal((vProd * 100) / vTotProd.doubleValue());
					}
					//BigDecimal percItem = new BigDecimal(getDouble(prod.getVProd(), 15, 2)).multiply(new BigDecimal(100)).divide(new BigDecimal(getDouble(total.getICMSTot().getVProd(), 15, 2, true)));
					
					if(vTotalDesconto.doubleValue() > 0){
						BigDecimal vDescIt = vTotalDesconto.multiply(percItem).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
						sTotalDesconto = sTotalDesconto.add(vDescIt);
						prod.setVDesc(getDouble(vDescIt.toString(), 15, 2));
					}
					if(vTotalFrete.doubleValue() > 0){
						BigDecimal vFreteIt = vTotalFrete.multiply(percItem).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
						sTotalFrete = sTotalFrete.add(vFreteIt);
						prod.setVFrete(getDouble(vFreteIt.toString(), 15, 2));
					}
					if(vTotalSeguro.doubleValue() > 0){
						BigDecimal vSeguroIt = vTotalSeguro.multiply(percItem).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
						sTotalSeguro = sTotalSeguro.add(vSeguroIt);
						prod.setVSeg(getDouble(vSeguroIt.toString(), 15, 2));
					}
					if(vTotalOutro.doubleValue() > 0){
						BigDecimal vOutroIt = vTotalOutro.multiply(percItem).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
						sTotalOutro = sTotalOutro.add(vOutroIt);
						prod.setVOutro(getDouble(vOutroIt.toString(), 15, 2));
					}
				}
				
				sTotalIcmsSt = sTotalIcmsSt.add(new BigDecimal(getDouble(rs.getString("VLRICMSSTITVENDA"), 15, 2, true)));
				sTotalIpi = sTotalIpi.add(new BigDecimal(getDouble(rs.getString("VLRIPIITVENDA"), 15, 2, true)));
				
				prod.setIndTot("1");

				// TODO - Implementações referente a dados de Importação de
				// produtos na NFe
				// NFe.InfNFe.Det.Prod.DI di = new
				// ObjectFactory().createTNFeInfNFeDetProdDI();

				// TODO - Implementações referente a Detalhamento Específico de
				// Veículos novos na NFe
				// TNFe.InfNFe.Det.Prod.VeicProd veicProd = new
				// ObjectFactory().createTNFeInfNFeDetProdVeicProd();

				// TODO - Implementações referente a Detalhamento Específico de
				// Medicamento e de matérias-primas farmacêuticas na NFe
				// TNFe.InfNFe.Det.Prod.Med med = new
				// ObjectFactory().createTNFeInfNFeDetProdMed();

				// TODO - Implementações referente a Detalhamento Específico de
				// armamentos na NFe
				// TNFe.InfNFe.Det.Prod.Arma arma = new
				// ObjectFactory().createTNFeInfNFeDetProdArma();

				// TODO - Implementações referente a Detalhamento Específico de
				// Combustível na NFe
				// TNFe.InfNFe.Det.Prod.Comb comb = new
				// ObjectFactory().createTNFeInfNFeDetProdComb();

				TNFe.InfNFe.Det.Imposto impostos = new ObjectFactory().createTNFeInfNFeDetImposto();
				
				if (rs.getString("TIPOPROD").trim().equals("S")) {
					//Informar somente para a NFe 2.00
					prod.setNCM(getString("99", 2, true));
					
					TNFe.InfNFe.Det.Imposto.ISSQN issqn = new ObjectFactory().createTNFeInfNFeDetImpostoISSQN();
					issqn.setVBC(getDouble(rs.getString("VLRBASEICMSITVENDA"), 15, 2, true));
					issqn.setVAliq(getDouble(rs.getString("PERCISSITVENDA"), 5, 2, true));
					issqn.setVISSQN(getDouble(rs.getString("VLRISSITVENDA"), 15, 2, true));
					issqn.setCMunFG(getString(ide.getCMunFG(), 7, true));
					issqn.setCListServ(getString(rs.getString("CODSERV"), 4, true, "."));
				} else {
					
					prod.setNCM(NFeUtil.tratarNcm(getString(rs.getString("CODNCM"), 8, true, ".")));
					
					TNFe.InfNFe.Det.Imposto.ICMS icms = new ObjectFactory().createTNFeInfNFeDetImpostoICMS();
					impostos.setICMS(icms);
					
					if(this.simples && emit.getCRT().equals("1")){
						
						BigDecimal vlrCred = new BigDecimal(0);
						
						String csosn = getString(rs.getString("CSOSN"), 3);
						if(csosn != null){
							
							if (csosn.equals("101")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMSSN101 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMSSN101();
								icms.setICMSSN101(tipoIcms);
								tipoIcms.setCSOSN(csosn);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setPCredSN(getDouble(new Double(this.aliqSimples).toString(), 5, 2, true));
								vlrCred = new BigDecimal(prod.getVProd()).multiply(new BigDecimal(this.aliqSimples).divide(new BigDecimal(100)));
								tipoIcms.setVCredICMSSN(getDouble(vlrCred.toString(), 15, 2));
							} else if (csosn.equals("102") || csosn.equals("103") || csosn.equals("300") || csosn.equals("400")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMSSN102 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMSSN102();
								icms.setICMSSN102(tipoIcms);
								tipoIcms.setCSOSN(csosn);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
							} else if (csosn.equals("201")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMSSN201 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMSSN201();
								icms.setICMSSN201(tipoIcms);
								tipoIcms.setCSOSN(csosn);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITVENDA"), 5, 2));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPCredSN(getDouble(new Double(this.aliqSimples).toString(), 5, 2, true));
								vlrCred = new BigDecimal(prod.getVProd()).multiply(new BigDecimal(this.aliqSimples).divide(new BigDecimal(100)));
								tipoIcms.setVCredICMSSN(getDouble(vlrCred.toString(), 15, 2));
							} else if (csosn.equals("202") || csosn.equals("203")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMSSN202 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMSSN202();
								icms.setICMSSN202(tipoIcms);
								tipoIcms.setCSOSN(csosn);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITVENDA"), 5, 2));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITVENDA"), 15, 2, true));
							} else if (csosn.equals("500")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMSSN500 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMSSN500();
								icms.setICMSSN500(tipoIcms);
								tipoIcms.setCSOSN(csosn);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								//tipoIcms.setVBCSTRet("0.00");
								//tipoIcms.setVICMSSTRet("0.00");
							} else if (csosn.equals("900")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMSSN900 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMSSN900();
								icms.setICMSSN900(tipoIcms);
								tipoIcms.setCSOSN(csosn);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setPRedBC(getDouble(rs.getString("REDFISC"), 5, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITVENDA"), 15, 2, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITVENDA"), 5, 2));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPCredSN(getDouble(new Double(this.aliqSimples).toString(), 5, 2, true));
								vlrCred = new BigDecimal(prod.getVProd()).multiply(new BigDecimal(this.aliqSimples).divide(new BigDecimal(100)));
								tipoIcms.setVCredICMSSN(getDouble(vlrCred.toString(), 15, 2));
							}
							
							vlrIcmsSimples = vlrIcmsSimples.add(vlrCred);
						}
						
					}else{
						
						String cst = getString(rs.getString("CODTRATTRIB"), 2);
						if(cst != null){
							if (cst.equals("00")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS00 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS00();
								icms.setICMS00(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITVENDA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITVENDA"), 15, 2, true));
							} else if (cst.equals("10")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS10 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS10();
								icms.setICMS10(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITVENDA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITVENDA"), 15, 2, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITVENDA"), 5, 2));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
							} else if (cst.equals("20")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS20 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS20();
								icms.setICMS20(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITVENDA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITVENDA"), 15, 2, true));
								tipoIcms.setPRedBC(getDouble(rs.getString("REDFISC"), 5, 2, true));
							} else if (cst.equals("30")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS30 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS30();
								icms.setICMS30(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setModBCST(getInteger(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITVENDA"), 5, 2));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
							} else if (cst.equals("40") || cst.equals("41") || cst.equals("50")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS40 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS40();
								icms.setICMS40(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								/*
								Usado somente para veiculos beneficiados com a desoneração condicional do ICMS, e especificar o motiva da desoneração
								tipoIcms.setVICMS(rs.getString("VLRICMSITVENDA"));
								
								Motivo da desoneração do ICMS
								tipoIcms.setMotDesICMS(rs.getString(null));
								 */
							} else if (cst.equals("51")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS51 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS51();
								icms.setICMS51(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITVENDA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITVENDA"), 15, 2, true));
								tipoIcms.setPRedBC(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
							} else if (cst.equals("60")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS60 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS60();
								icms.setICMS60(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setOrig(getString(rs.getString("ORIGFISC")));
								//tipoIcms.setVBCSTRet("0.00");
								//tipoIcms.setVICMSSTRet("0.00");
							} else if (cst.equals("70")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS70 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS70();
								icms.setICMS70(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITVENDA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITVENDA"), 15, 2, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITVENDA"), 5, 2));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPRedBC(getDouble(rs.getString("REDFISC"), 5, 2, true));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, true));
							} else if (cst.equals("90")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS90 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS90();
								icms.setICMS90(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITVENDA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITVENDA"), 15, 2, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITVENDA"), 5, 2));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITVENDA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITVENDA"), 15, 2, true));
								tipoIcms.setPRedBC(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
							}
						}
					}
					
					String cstIpi = getString(rs.getString("CODSITTRIBIPI"), 2);
					if(cstIpi != null){
						TNFe.InfNFe.Det.Imposto.IPI ipi = new ObjectFactory().createTNFeInfNFeDetImpostoIPI();
						// TODO - Cód. enquadramento legal do ipi. Tabela a ser criada pela RFB, informar 999 enquanto a tabela
						ipi.setCEnq(getString("999", 3, true));
						if (cstIpi.matches("^(01|02|03|04|51|52|53|54|54)")) {
							TNFe.InfNFe.Det.Imposto.IPI.IPINT ipiNT = new ObjectFactory().createTNFeInfNFeDetImpostoIPIIPINT();
							ipi.setIPINT(ipiNT);
							ipiNT.setCST(cstIpi);
						} else if (cstIpi.matches("^(00|49|50|99)")) {
							TNFe.InfNFe.Det.Imposto.IPI.IPITrib ipiTrib = new ObjectFactory().createTNFeInfNFeDetImpostoIPIIPITrib();
							ipi.setIPITrib(ipiTrib);
							ipiTrib.setCST(cstIpi);
							if (rs.getString("TPCALCIPI").equals("P")) {
								ipiTrib.setPIPI(getDouble(rs.getString("PERCIPIITVENDA"), 5, 2, true));
								ipiTrib.setVBC(getDouble(rs.getString("VLRBASEIPIITVENDA"), 15, 2, true));
							} else if (rs.getString("TPCALCIPI").equals("V")) {
								ipiTrib.setQUnid(getDouble(rs.getString("QTDITVENDA"),16, 4, true));
								ipiTrib.setVUnid(getDouble(rs.getString("VLRIPIUNIDTRIB"), 15, 2, true));
							}
							ipiTrib.setVIPI(getDouble(rs.getString("VLRIPIITVENDA"),15, 2, true));
						}
						impostos.setIPI(ipi);
					}
					
					// TODO - Impostos de Importação
					// TNFe.InfNFe.Det.Imposto.II ii = new
					// ObjectFactory().createTNFeInfNFeDetImpostoII();
	
					String cstPis = getString(rs.getString("CODSITTRIBPIS"), 2);
					if(cstPis != null){
						TNFe.InfNFe.Det.Imposto.PIS pis = new ObjectFactory().createTNFeInfNFeDetImpostoPIS();
						if (cstPis.matches("^(01|02)")) {
							TNFe.InfNFe.Det.Imposto.PIS.PISAliq pisAliq = new ObjectFactory().createTNFeInfNFeDetImpostoPISPISAliq();
							pis.setPISAliq(pisAliq);
							pisAliq.setCST(cstPis);
							pisAliq.setVBC(getDouble(rs.getString("VLRPRODITVENDA"), 15, 2, true));
							pisAliq.setPPIS(getDouble(rs.getString("ALIQPISFISC"), 5, 2, true));
							pisAliq.setVPIS(getDouble(String.valueOf((rs.getDouble("VLRPRODITVENDA") * rs.getDouble("ALIQPISFISC")) / 100), 15, 2, true));
						} else if (cstPis.matches("^(03)")) {
							TNFe.InfNFe.Det.Imposto.PIS.PISQtde pisQtde = new ObjectFactory().createTNFeInfNFeDetImpostoPISPISQtde();
							pis.setPISQtde(pisQtde);
							pisQtde.setCST(cstPis);
							pisQtde.setQBCProd(getDouble(rs.getString("QTDITVENDA"), 16, 4, true));
							pisQtde.setVAliqProd(getDouble(rs.getString("VLRPISUNIDTRIB"), 15, 4, true));
							pisQtde.setVPIS(getDouble(String.valueOf(rs.getDouble("VLRPISUNIDTRIB") * rs.getDouble("QTDITVENDA")), 15, 2, true));
						} else if (cstPis.matches("^(04|06|07|08|09)")) {
							TNFe.InfNFe.Det.Imposto.PIS.PISNT pisNT = new ObjectFactory().createTNFeInfNFeDetImpostoPISPISNT();
							pis.setPISNT(pisNT);
							pisNT.setCST(cstPis);
						} else if (cstPis.matches("^(99)")) {
							TNFe.InfNFe.Det.Imposto.PIS.PISOutr pisOutr = new ObjectFactory().createTNFeInfNFeDetImpostoPISPISOutr();
							pis.setPISOutr(pisOutr);
							pisOutr.setCST(cstPis);
							// TODO - Não está implementado no freedom-erp Tipo de calc
							// PIS (Percentual ou Valor)
							// if(rs.getString("TPCALCPIS").equals("P")){
							pisOutr.setVBC(getDouble(rs.getString("VLRPRODITVENDA"), 15, 2, true));
							pisOutr.setPPIS(getDouble(rs.getString("ALIQPISFISC"), 5, 2, true));
							pisOutr.setVPIS(getDouble(String.valueOf((rs.getDouble("VLRPRODITVENDA") * rs.getDouble("ALIQPISFISC")) / 100), 15, 2, true));
							// }else if(rs.getString("TPCALCPIS").equals("V")){
							// pisOutr.setQBCProd(rs.getString("QTDITVENDA"));
							// pisOutr.setVAliqProd(rs.getString("VLRPISUNIDTRIB"));
							// pisOutr.setVPIS(String.valueOf(rs.getDouble("VLRPISUNIDTRIB")
							// * rs.getDouble("QTDITVENDA")));
							// }
						}
						impostos.setPIS(pis);
					}
					
					// TODO - PIS Substituição Tributária
					// TNFe.InfNFe.Det.Imposto.PISST pisST = new ObjectFactory().createTNFeInfNFeDetImpostoPISST();
					// impostos.setPISST(pisST);
					
					String cstCof = getString(rs.getString("CODSITTRIBCOF"), 2);
					if(cstCof != null){
						TNFe.InfNFe.Det.Imposto.COFINS cofins = new ObjectFactory().createTNFeInfNFeDetImpostoCOFINS();
						if (cstCof.matches("^(01|02)")) {
							TNFe.InfNFe.Det.Imposto.COFINS.COFINSAliq cofinsAliq = new ObjectFactory().createTNFeInfNFeDetImpostoCOFINSCOFINSAliq();
							cofins.setCOFINSAliq(cofinsAliq);
							cofinsAliq.setCST(cstCof);
							cofinsAliq.setVBC(getDouble(rs.getString("VLRPRODITVENDA"), 15, 2, true));
							cofinsAliq.setPCOFINS(getDouble(rs.getString("ALIQCOFINSFISC"), 5, 2, true));
							cofinsAliq.setVCOFINS(getDouble(String.valueOf((rs.getDouble("VLRPRODITVENDA") * rs.getDouble("ALIQCOFINSFISC")) / 100), 15, 2, true));
						} else if (cstCof.matches("^(03)")) {
							TNFe.InfNFe.Det.Imposto.COFINS.COFINSQtde cofinsQtde = new ObjectFactory().createTNFeInfNFeDetImpostoCOFINSCOFINSQtde();
							cofins.setCOFINSQtde(cofinsQtde);
							cofinsQtde.setCST(cstCof);
							cofinsQtde.setQBCProd(getDouble(rs.getString("QTDITVENDA"), 16, 4, true));
							cofinsQtde.setVAliqProd(getDouble(rs.getString("VLRCOFUNIDTRIB"), 15, 4, true));
							cofinsQtde.setVCOFINS(getDouble(String.valueOf(rs.getDouble("VLRCOFUNIDTRIB") * rs.getDouble("QTDITVENDA")), 15, 2, true));
						} else if (cstCof.matches("^(04|06|07|08|09)")) {
							TNFe.InfNFe.Det.Imposto.COFINS.COFINSNT cofinsNT = new ObjectFactory().createTNFeInfNFeDetImpostoCOFINSCOFINSNT();
							cofins.setCOFINSNT(cofinsNT);
							cofinsNT.setCST(cstCof);
						} else if (cstCof.matches("^(99)")) {
							TNFe.InfNFe.Det.Imposto.COFINS.COFINSOutr cofinsOutr = new ObjectFactory().createTNFeInfNFeDetImpostoCOFINSCOFINSOutr();
							cofins.setCOFINSOutr(cofinsOutr);
							cofinsOutr.setCST(cstCof);
							// TODO - Não está implementado no freedom-erp Tipo de calc
							// PIS (Percentual ou Valor)
							// if(rs.getString("TPCALCCOF").equals("P")){
							cofinsOutr.setVBC(getDouble(rs.getString("VLRPRODITVENDA"), 15, 2, true));
							cofinsOutr.setPCOFINS(getDouble(rs.getString("ALIQCOFINSFISC"), 5, 2, true));
							cofinsOutr.setVCOFINS(getDouble(String.valueOf((rs.getDouble("VLRPRODITVENDA") * rs.getDouble("ALIQCOFINSFISC")) / 100), 15, 2, true));
							// }else if(rs.getString("TPCALCCOF").equals("V")){
							// cofinsOutr.setQBCProd(rs.getString("QTDITVENDA"));
							// cofinsOutr.setVAliqProd(rs.getString("VLRCOFUNIDTRIB"));
							// cofinsOutr.setVCOFINS(String.valueOf(rs.getDouble("VLRPISUNIDTRIB")
							// * rs.getDouble("QTDITVENDA")));
							// }
						}
						impostos.setCOFINS(cofins);
					}
					
					// TODO - Cofins Substituição Tributária
					// TNFe.InfNFe.Det.Imposto.COFINSST cofinsST = new
					// ObjectFactory().createTNFeInfNFeDetImpostoCOFINSST();
					// impostos.setCOFINSST(cofinsST);
					
				}
				
				det.setProd(prod);
				det.setImposto(impostos);
				infNFe.getDet().add(det);
			}
			
			if(vTotalDesconto.doubleValue() != sTotalDesconto.doubleValue()){
				BigDecimal difDesc = vTotalDesconto.subtract(sTotalDesconto);
				sTotalDesconto = sTotalDesconto.add(difDesc);
				prod.setVDesc(getDouble(new BigDecimal(prod.getVDesc()).add(difDesc).toString(), 15, 2, true));
			}
			if(vTotalFrete.doubleValue() != sTotalFrete.doubleValue()){
				BigDecimal difFrete = vTotalFrete.subtract(sTotalFrete);
				sTotalFrete = sTotalFrete.add(difFrete);
				prod.setVFrete(getDouble(new BigDecimal(prod.getVFrete()).add(difFrete).toString(), 15, 2, true));
			}
			if(vTotalSeguro.doubleValue() != sTotalSeguro.doubleValue()){
				BigDecimal difSeg = vTotalSeguro.subtract(sTotalSeguro);
				sTotalSeguro = sTotalSeguro.add(difSeg);
				prod.setVSeg(getDouble(new BigDecimal(prod.getVSeg()).add(difSeg).toString(), 15, 2, true));
			}
			if(vTotalOutro.doubleValue() != sTotalOutro.doubleValue()){
				BigDecimal difOutro = vTotalOutro.subtract(sTotalOutro);
				sTotalOutro = sTotalOutro.add(difOutro);
				prod.setVOutro(getDouble(new BigDecimal(prod.getVOutro()).add(difOutro).toString(), 15, 2, true));
			}
			
			total.getICMSTot().setVDesc(getDouble(sTotalDesconto.toString(), 15, 2, true));
			total.getICMSTot().setVFrete(getDouble(sTotalFrete.toString(), 15, 2, true));
			total.getICMSTot().setVSeg(getDouble(sTotalSeguro.toString(), 15, 2, true));
			total.getICMSTot().setVOutro(getDouble(sTotalOutro.toString(), 15, 2, true));
			
			BigDecimal vNF = new BigDecimal(total.getICMSTot().getVProd()).subtract(sTotalDesconto).add(sTotalIcmsSt).add(sTotalFrete).add(sTotalSeguro).add(sTotalOutro).add(sTotalIpi);
			total.getICMSTot().setVNF(getDouble(vNF.toString(), 15, 2, true));
			
			if(this.simples && vlrIcmsSimples.doubleValue() > 0){
				setInfAdFisco(NFeUtil.geraMensagens(this.msgSimples, getDouble(vlrIcmsSimples.toString(), 15, 2, true), getDouble(new BigDecimal(this.aliqSimples).toString(), 15, 2, true)));
			}
			
			conSys.commit();
			
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null, "Erro ao carregar itens da venda!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}
	}
	
	protected void carregaInfTransporte() {
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT tra.RAZTRAN, tra.CNPJTRAN, tra.CPFTRAN, tra.INSCTRAN, tra.ENDTRAN, tra.CIDTRAN, tra.UFTRAN, tra.CODMUNIC, tra.EMAILNFETRAN, uf.CODUF, "
				+ "fre.TIPOFRETEVD, fre.CONHECFRETEVD, fre.PLACAFRETEVD, fre.UFFRETEVD, fre.QTDFRETEVD, fre.PESOBRUTVD, fre.PESOLIQVD, fre.ESPFRETEVD, fre.MARCAFRETEVD, "
				+ "cfre.VLRFRETE, cfre.ALIQICMSFRETE, cfre.VLRICMSFRETE, cfre.VLRBASEICMSFRETE, cfre.CODNAT "
				+ "FROM VDFRETEVD fre LEFT JOIN VDTRANSP tra ON (fre.CODTRAN = tra.CODTRAN) LEFT JOIN LFFRETE cfre ON (fre.CONHECFRETEVD = cfre.CODFRETE) "
				+ "LEFT JOIN SGUF uf ON (tra.SIGLAUF = uf.SIGLAUF) "
				+ "WHERE fre.CODEMP=? AND fre.CODFILIAL=? AND fre.CODVENDA=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODVENDA));
			rs = ps.executeQuery();

			if (rs.next()) {
				transp = new ObjectFactory().createTNFeInfNFeTransp();
				infNFe.setTransp(transp);
				if (rs.getString("TIPOFRETEVD").trim().equals("C")) {
					transp.setModFrete(getInteger("0", 1, true)); // CIF - Frete por conta do emitente
				} else if (rs.getString("TIPOFRETEVD").trim().equals("F")) {
					transp.setModFrete(getInteger("1", 1, true)); // FOB - Frete por conta do destinatário
				}
				TNFe.InfNFe.Transp.Transporta transporta = new ObjectFactory().createTNFeInfNFeTranspTransporta();
				transp.setTransporta(transporta);
				if (rs.getString("CNPJTRAN") != null) {
					transporta.setCNPJ(getString(rs.getString("CNPJTRAN"), 14, true, "./-"));
				} else if (rs.getString("CPFTRAN") != null) {
					transporta.setCPF(getString(rs.getString("CPFTRAN"), 11, true, "./-"));
				}
				transporta.setXNome(getString(rs.getString("RAZTRAN"), 60));
				transporta.setIE(getString(rs.getString("INSCTRAN"), 14, false, "./-"));
				transporta.setXEnder(getString(rs.getString("ENDTRAN"), 60));
				transporta.setXMun(getString(rs.getString("CIDTRAN"), 60));
				transporta.setUF(getTUf(rs.getString("UFTRAN")));
				
				if(rs.getString("EMAILNFETRAN") != null){
					setEmailsNfe(getString(rs.getString("EMAILNFETRAN")), "TRANSPORTADORA");
				}

				if (rs.getString("CONHECFRETEVD") != null) {
					TNFe.InfNFe.Transp.RetTransp retTransp = new ObjectFactory().createTNFeInfNFeTranspRetTransp();
					transp.setRetTransp(retTransp);
					retTransp.setVServ(getDouble(rs.getString("VLRFRETE"), 15, 2, true));
					retTransp.setVBCRet(getDouble(rs.getString("VLRBASEICMSFRETE"), 15, 2, true));
					retTransp.setPICMSRet(getDouble(rs.getString("ALIQICMSFRETE"), 5, 2, true));
					retTransp.setVICMSRet(getDouble(rs.getString("VLRICMSFRETE"), 15, 2, true));
					retTransp.setCFOP(getInteger(rs.getString("CODNAT"), 4, true));
					retTransp.setCMunFG(getString(rs.getString("CODUF"), 2, true) + getString(rs.getString("CODMUNIC"), 5, true));
				}
				TVeiculo veicTransp = new ObjectFactory().createTVeiculo();
				transp.setVeicTransp(veicTransp);
				String placaFrete = rs.getString("PLACAFRETEVD").replace("-", "").replace("*", "").replace(" ", "");
				if(placaFrete.length() == 7){
					veicTransp.setPlaca(getString(placaFrete, 7, true));
				}else{
					veicTransp.setPlaca(getString("XXX9999", 7, true));
				}
				TUf ufVeicTransp = getTUf(rs.getString("UFFRETEVD"));
				if(ufVeicTransp != null){
					veicTransp.setUF(ufVeicTransp);
				}else{
					veicTransp.setUF(getTUf(emit.getEnderEmit().getUF().value()));
				}
				// veicTransp.setRNTC(); 376 | X21 - Registro nacional de
				// veiculos de carga. Não implementado no Freedom-erp -
				// 4.01-NT2009.006x

				TNFe.InfNFe.Transp.Vol vol = new ObjectFactory().createTNFeInfNFeTranspVol();
				transp.getVol().add(vol);
				vol.setQVol(getInteger(rs.getString("QTDFRETEVD"), 15));
				vol.setEsp(getString(rs.getString("ESPFRETEVD"), 60));
				vol.setMarca(getString(rs.getString("MARCAFRETEVD"), 60));
				vol.setPesoB(getDouble(rs.getString("PESOBRUTVD"), 15, 3));
				vol.setPesoL(getDouble(rs.getString("PESOLIQVD"), 15, 3));
			}
			
			conSys.commit();
			
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null, "Erro ao carregar informações de transporte!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}
	}

	private void carregaCobranca() {
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT CODREC, VLRREC, VLRDESCREC, VLRAPAGREC "
				+ "FROM FNRECEBER WHERE CODEMP=? AND CODFILIAL=? AND CODVENDA=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODVENDA));
			rs = ps.executeQuery();

			if (rs.next()) {
				TNFe.InfNFe.Cobr cobr = new ObjectFactory()
						.createTNFeInfNFeCobr();
				infNFe.setCobr(cobr);
				TNFe.InfNFe.Cobr.Fat fat = new ObjectFactory()
						.createTNFeInfNFeCobrFat();
				cobr.setFat(fat);
				fat.setNFat(getString(rs.getString("CODREC"), 60));
				fat.setVOrig(getDouble(rs.getString("VLRREC"), 15, 2, false, true));
				fat.setVDesc(getDouble(rs.getString("VLRDESCREC"), 15, 2, false, true));
				fat.setVLiq(getDouble(rs.getString("VLRAPAGREC"), 15, 2, false, true));
				int codRec = rs.getInt("CODREC");
				
				conSys.commit();

				sql = "SELECT NPARCITREC, DTVENCITREC, VLRPARCITREC "
						+ "FROM FNITRECEBER WHERE CODEMP=? AND CODFILIAL=? AND CODREC=?";
				ps = conSys.prepareStatement(sql);
				ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
				ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
				ps.setInt(3, codRec);
				rs = ps.executeQuery();
				while (rs.next()) {
					TNFe.InfNFe.Cobr.Dup dup = new ObjectFactory().createTNFeInfNFeCobrDup();
					dup.setNDup(getString(rs.getString("NPARCITREC"), 60));
					dup.setDVenc(getDate(rs.getString("DTVENCITREC")));
					dup.setVDup(getDouble(rs.getString("VLRPARCITREC"), 15, 2, false, true));
					cobr.getDup().add(dup);
				}
			}
			
			conSys.commit();
			
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao carregar informações de cobrança!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}
	}

	@Override
	protected void setStatusNFe(String pathXML){
		String sql;
		PreparedStatement ps;
		
		if(chaveNfe != null && chaveNfe.trim().length() > 0){
			sql = "UPDATE VDVENDA SET CHAVENFEVENDA = ? WHERE CODEMP=? AND CODFILIAL=? AND CODVENDA=? AND TIPOVENDA=?";
			try {
				ps = conSys.prepareStatement(sql);
				ps.setString(1, chaveNfe);
				
				/* Alterado para testes
				if(chaveNfe != null && chaveNfe.trim().length() > 0){
					ps.setString(1, chaveNfe);
				}else{
					ps.setNull(1, Types.CHAR);
				}
				*/
				ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODEMP));
				ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODFILIAL));
				ps.setInt(4, (Integer) key.get(FreedomNFEKey.CODVENDA));
				ps.setString(5, "V");
				ps.execute();
				conSys.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		sql = "EXECUTE PROCEDURE VDADICNFEOPERASP(?,?,?,?,?,?)";
		try {
			ps = conNFE.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODVENDA));
			ps.setString(4, "V");
			if(chaveNfe != null && chaveNfe.trim().length() > 0){
				ps.setString(5, chaveNfe);
			}else{
				ps.setNull(5, Types.CHAR);
			}
			if(pathXML != null && pathXML.trim().length() > 0){
				ps.setString(6, pathXML);
			}else{
				ps.setNull(6, Types.CHAR);
			}
			ps.execute();
			
			conNFE.commit();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
