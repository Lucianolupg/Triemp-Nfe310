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

public class NFeCompra extends NFe {
	private TNFe.InfNFe.Det.Prod.DI di;
	
	public NFeCompra(DbConnection conSys, DbConnection conNFE, AbstractNFEKey key) {
		super(conSys, conNFE, key);
		carregaCompra();
		carregaInfoNFe();
		carregaItCompra();
		carregaInfTransporte();
		carregaCobranca();
		this.carregaXmlNFe();
	}
	
	public boolean carregaXmlNFe(){
		String chave = "";
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT COALESCE(CHAVENFECOMPRA,'') AS CHAVENFE FROM CPCOMPRA WHERE CODEMP=? AND CODFILIAL=? AND CODCOMPRA=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODCOMPRA));
			rs = ps.executeQuery();

			if (rs.next()) {
				chave = rs.getString("CHAVENFE").trim();
			}
			conSys.commit();
			
			if(chave.length() > 0){
				sql = "SELECT COALESCE(PATHNFE,'') as PATHNFE FROM CPNFE WHERE CODEMP=? AND CODFILIAL=? AND CODCOMPRA=?"; // AND CHAVENFE=?";
				ps = conNFE.prepareStatement(sql);
				ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
				ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
				ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODCOMPRA));
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

	private boolean carregaCompra() {
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT FI.RAZFILIAL, FI.NOMEFILIAL, FI.CNPJFILIAL, FI.INSCFILIAL, FI.INSCMUNFILIAL, FI.CNAEFILIAL, FI.DDDFILIAL, FI.FONEFILIAL, FI.ENDFILIAL, FI.COMPLFILIAL, FI.NUMFILIAL, FI.BAIRFILIAL, FI.CEPFILIAL, FI.CODMUNIC, mu.NOMEMUNIC, uf.CODUF, uf.SIGLAUF, FI.CODPAIS, pa.NOMEPAIS, pa.CODBACENPAIS, FI.PERCPISFILIAL, FI.PERCCOFINSFILIAL, FI.PERCIRFILIAL, FI.PERCCSOCIALFILIAL, FI.SIMPLESFILIAL, FI.PERCSIMPLESFILIAL, " 
				+ "CP.CHAVENFECOMPRA, CP.OBSERVACAO, CP.CODFOR, CP.CODPLANOPAG, CP.DTEMITCOMPRA, CP.DTENTCOMPRA, CP.SERIE, CP.DOCCOMPRA, CP.VLRBASEICMSCOMPRA, CP.VLRICMSCOMPRA, CP.VLRBASEICMSSTCOMPRA, CP.VLRICMSSTCOMPRA, CP.VLRPRODCOMPRA, CP.VLRFRETECOMPRA, CP.VLRDESCCOMPRA, CP.VLRIPICOMPRA, CP.VLRPISCOMPRA, CP.VLRCOFINSCOMPRA, CP.VLRADICCOMPRA, CP.VLRLIQCOMPRA, "
				+ "CP.NRODI, CP.DTREGDI, CP.LOCDESEMBDI, CP.SIGLAUFDESEMBDI, CP.CODPAISDESEMBDI, CP.DTDESEMBDI, CP.IDENTCONTAINER "
				+ "FROM CPCOMPRA CP INNER JOIN SGFILIAL FI ON (CP.CODFILIAL = FI.CODFILIAL) INNER JOIN SGUF UF ON (FI.SIGLAUF = UF.SIGLAUF) INNER JOIN SGMUNICIPIO MU ON (FI.CODMUNIC = MU.CODMUNIC AND UF.SIGLAUF = MU.SIGLAUF) INNER JOIN SGPAIS PA ON (FI.CODPAIS = PA.CODPAIS) "
				+ "WHERE CP.CODEMP=? AND CP.CODFILIAL=? AND CP.CODCOMPRA=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODCOMPRA));
			rs = ps.executeQuery();

			if (rs.next()) {
				// Testa se é optante pelo simples
				if(getString(rs.getString("SIMPLESFILIAL")).equals("S")){
					this.simples = true;
					this.aliqSimples = rs.getDouble("PERCSIMPLESFILIAL");
				}

				if(rs.getString("NRODI") != null){
					di = new ObjectFactory().createTNFeInfNFeDetProdDI();
					di.setNDI(getString(rs.getString("NRODI"), 10, true));
					di.setDDI(getDate(rs.getString("DTREGDI"), true));
					di.setXLocDesemb(getString(rs.getString("LOCDESEMBDI"), 60, true));
					di.setUFDesemb(getTUfEmi(rs.getString("SIGLAUFDESEMBDI")));
					di.setDDesemb(getDate(rs.getString("DTDESEMBDI"), true));
					
					TNFe.InfNFe.Det.Prod.DI.Adi adi = new ObjectFactory().createTNFeInfNFeDetProdDIAdi();
					di.getAdi().add(adi);
					
					adi.setNAdicao("1");
					adi.setNSeqAdic("1");
					adi.setCFabricante("000");
				}
				ide.setCUF(getInteger(rs.getString("CODUF"), 2, true));
				ide.setIndPag(getInteger(getFormaPagamento(rs.getInt("CODPLANOPAG")), 1, true));
				ide.setMod(getString("55", 2, true));
				ide.setSerie(getString(rs.getString("SERIE"), 3, true));
				ide.setNNF(getInteger(rs.getString("DOCCOMPRA"), 9, true));
				ide.setDEmi(getDate(rs.getString("DTEMITCOMPRA"), true));
				ide.setDSaiEnt(getDate(rs.getString("DTENTCOMPRA")));
				ide.setTpNF(getString("0", 1, true)); // 0 - TIPO NOTA DE COMPRA
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
				endEmit.setCEP(getString(rs.getString("CEPFILIAL"), 8, false, ".-"));
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
				
				icmsTot.setVBC(getDouble(rs.getString("VLRBASEICMSCOMPRA"), 15, 2, true));
				icmsTot.setVICMS(getDouble(rs.getString("VLRICMSCOMPRA"), 15, 2, true));
				icmsTot.setVBCST(getDouble(rs.getString("VLRBASEICMSSTCOMPRA"), 15, 2, true));
				icmsTot.setVST(getDouble(rs.getString("VLRICMSSTCOMPRA"), 15, 2, true));
				icmsTot.setVProd(getDouble(rs.getString("VLRPRODCOMPRA"), 15, 2, true));
				icmsTot.setVFrete(getDouble(rs.getString("VLRFRETECOMPRA"), 15, 2));
				icmsTot.setVSeg(getDouble("0", 15, 2)); // Valor total do seguro. Não implementado no Freedom-erp
				icmsTot.setVDesc(getDouble(rs.getString("VLRDESCCOMPRA"), 15, 2));
				icmsTot.setVII(getDouble("0", 15, 2)); // Valor total do Imposto de Importação. Não implementado no Freedom-erp
				icmsTot.setVIPI(getDouble(rs.getString("VLRIPICOMPRA"), 15, 2, true));
				icmsTot.setVPIS(getDouble(rs.getString("VLRPISCOMPRA"), 15, 2, true));
				icmsTot.setVCOFINS(getDouble(rs.getString("VLRCOFINSCOMPRA"), 15, 2, true));
				icmsTot.setVOutro(getDouble(rs.getString("VLRADICCOMPRA"), 15, 2, true));
				icmsTot.setVNF(getDouble(rs.getString("VLRLIQCOMPRA"), 15, 2, true));

				/* TODO - ISS NÃO IMPLEMENTADO NA COMPRA DO FREEDOM
				if(rs.getDouble("VLRBASEISSCOMPRA") > 0){
					TNFe.InfNFe.Total.ISSQNtot issqnTot = new ObjectFactory().createTNFeInfNFeTotalISSQNtot();
					total.setISSQNtot(issqnTot);
					
					issqnTot.setVServ(getDouble(rs.getString("VLRBASEISSCOMPRA"),15, 2)); // Verificar 343 | W18 - 4.01-NT2009.006x
					issqnTot.setVBC(getDouble(rs.getString("VLRBASEISSCOMPRA"), 15, 2));
					issqnTot.setVISS(getDouble(rs.getString("VLRISSCOMPRA"), 15, 2));
					issqnTot.setVPIS(getDouble(String.valueOf((rs.getDouble("VLRBASEISSCOMPRA") * rs.getDouble("PERCPISFOR") / 100)), 15, 2));
					issqnTot.setVCOFINS(getDouble(String.valueOf((rs.getDouble("VLRBASEISSCOMPRA") * rs.getDouble("PERCCOFINSFOR") / 100)), 15, 2));
				}
				*/
				this.setInfAdic(getString(rs.getString("OBSERVACAO")));
				
				/*
				 * TODO - 348 | W23 - Grupo de Retenção de tributos - 4.01-NT2009.006x
				 * TNFe.InfNFe.Total.RetTrib retTrib = new ObjectFactory().createTNFeInfNFeTotalRetTrib();
				 * total.setRetTrib(retTrib); //retTrib.setVRetPIS();
				 * retTrib.setVRetCOFINS(); retTrib.setVRetCSLL();
				 * retTrib.setVBCIRRF(); retTrib.setVIRRF();
				 * retTrib.setVRetPrev();
				 */
				
				carregaDestinatarioNF(rs.getString("CODFOR"));
			}
			
			conSys.commit();
			
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao carregar informações da compra!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
			
		}
		return true;
	}

	private void carregaDestinatarioNF(String codFor) {
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT FO.RAZFOR, FO.NOMEFOR, FO.PESSOAFOR, FO.CNPJFOR, FO.INSCFOR, FO.CPFFOR, FO.RGFOR, FO.SUFRAMAFOR, FO.ENDFOR, FO.COMPLFOR, FO.NUMFOR, FO.BAIRFOR, FO.CEPFOR, FO.CODMUNIC, MU.NOMEMUNIC, " 
				+ "UF.CODUF, UF.SIGLAUF, FO.CODPAIS, PA.NOMEPAIS, PA.CODBACENPAIS, FO.DDDFONEFOR, FO.FONEFOR, FO.EMAILFOR "
				+ "FROM CPFORNECED FO INNER JOIN SGUF UF ON (FO.SIGLAUF = UF.SIGLAUF) INNER JOIN SGMUNICIPIO MU ON (FO.CODMUNIC = MU.CODMUNIC AND UF.SIGLAUF = MU.SIGLAUF) INNER JOIN SGPAIS PA ON (FO.CODPAIS = PA.CODPAIS) "
				+ "WHERE FO.CODEMP=? AND FO.CODFILIAL=? AND FO.CODFOR=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, Integer.parseInt(codFor));
			rs = ps.executeQuery();
			if (rs.next()) {
				if(di != null){
					di.setCExportador(getString(rs.getString("RAZFOR"), 60, true));
				}
				dest.setXNome(getString(rs.getString("RAZFOR"), 60, true));
				if (rs.getString("PESSOAFOR").trim().equals("J")) {
					dest.setCNPJ(getString(rs.getString("CNPJFOR"), 14, true, "./-"));
				} else if (rs.getString("PESSOAFOR").trim().equals("F")) {
					dest.setCPF(getString(rs.getString("CPFFOR"), 11, true, "./-"));
				}
				if(rs.getString("INSCFOR") == null){
					dest.setIE(getString("", 14, false, "./-"));
				}else{
					dest.setIE(getString(rs.getString("INSCFOR"), 14, false, "./-"));
				}
				dest.setISUF(getString(rs.getString("SUFRAMAFOR"), 9));
				
				endDest.setXLgr(getString(rs.getString("ENDFOR"), 60, true));
				endDest.setNro(getString(rs.getString("NUMFOR"), 60, true));
				endDest.setXCpl(getString(rs.getString("COMPLFOR"), 60));
				endDest.setXBairro(getString(rs.getString("BAIRFOR"), 60, true));
				endDest.setCEP(getString(rs.getString("CEPFOR"), 8, false, "-"));
				endDest.setCMun(getString(rs.getString("CODUF"), 2, true) + getString(rs.getString("CODMUNIC"), 5, true));
				endDest.setXMun(getString(rs.getString("NOMEMUNIC"), 60, true));
				endDest.setUF(getTUf(rs.getString("SIGLAUF")));
				endDest.setCPais(getString(rs.getString("CODBACENPAIS"), 4));
				endDest.setXPais(getString(rs.getString("NOMEPAIS"), 60));
				if (rs.getString("FONEFOR") != null) {
					endDest.setFone(getString(rs.getString("DDDFONEFOR"), 2, false, "()") + getString(rs.getString("FONEFOR"), 14, false, "-"));
				}
			}
			
			conSys.commit();
			
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null, "Erro ao carregar informações da fornecedor (destinatário da NFe)!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}
	}

	private void carregaItCompra() {
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT PRO.CODEANPROD, PRO.DESCPROD, PRO.CODUNID, PRO.TIPOPROD, NATOP.DESCNAT, CLF.CODFISC, CLF.CODNCM, CLF.CODSERV, " 
				+ "CLFIT.CODITFISC, CLFIT.CODTRATTRIB, CLFIT.ORIGFISC, CLFIT.MODBCICMS, CLFIT.MODBCICMSST,  CLFIT.REDFISC, CLFIT.REDBASEST, CLFIT.CODSITTRIBIPI, CLFIT.TPCALCIPI, CLFIT.VLRIPIUNIDTRIB, CLFIT.CODSITTRIBPIS, CLFIT.ALIQPISFISC, CLFIT.VLRPISUNIDTRIB, CLFIT.CODSITTRIBCOF, CLFIT.ALIQCOFINSFISC, CLFIT.VLRCOFUNIDTRIB, CLFIT.CSOSN, "
				+ "NULL AS VLRBASEICMSSTITCOMPRA, NULL AS VLRICMSSTITCOMPRA, NULL AS MARGEMVLAGRITCOMPRA, CLFIT.TIPOFISC, CLFIT.TIPOST, " // TODO - Inserido temporariamente para compatibilidade, até arrumar caculos de ICMS ST na compra - Paulo Bueno
				+ "IT.CODEMP, IT.CODFILIAL, IT.CODCOMPRA, IT.CODITCOMPRA, IT.CODNAT, IT.CODPROD, IT.CODLOTE, IT.CODALMOX, IT.QTDITCOMPRA, IT.PRECOITCOMPRA, IT.PERCDESCITCOMPRA, IT.VLRDESCITCOMPRA, IT.PERCICMSITCOMPRA, IT.VLRBASEICMSITCOMPRA, IT.VLRICMSITCOMPRA, IT.PERCIPIITCOMPRA, IT.VLRBASEIPIITCOMPRA, IT.VLRIPIITCOMPRA, IT.VLRLIQITCOMPRA, IT.VLRADICITCOMPRA, IT.VLRFRETEITCOMPRA, IT.VLRPRODITCOMPRA, IT.VLRISENTASITCOMPRA, IT.VLROUTRASITCOMPRA, IT.REFPROD " 
				+ "FROM CPITCOMPRA IT INNER JOIN EQPRODUTO PRO ON (PRO.CODPROD = IT.CODPROD) LEFT JOIN LFNATOPER NATOP ON (NATOP.CODNAT = IT.CODNAT) LEFT JOIN LFCLFISCAL CLF ON (CLF.CODFISC = PRO.CODFISC) LEFT JOIN LFITCLFISCAL CLFIT ON (CLF.CODFISC = CLFIT.CODFISC AND CLFIT.CODITFISC = IT.CODITFISC) " 
				+ "WHERE IT.CODEMP=? AND IT.CODFILIAL=? AND IT.CODCOMPRA=? ORDER BY IT.CODITCOMPRA";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODCOMPRA));
			rs = ps.executeQuery();
			int nItem = 0;
			while (rs.next()) {
				if(ide.getNatOp() == null){
					//ide.setNatOp(getString(rs.getString("CODNAT"), 60, true)); // Código da CFOP
					ide.setNatOp(getString(rs.getString("DESCNAT"), 60, true)); // Descrição da CFOP
				}
				TNFe.InfNFe.Det det = new ObjectFactory().createTNFeInfNFeDet();
				TNFe.InfNFe.Det.Prod prod = new ObjectFactory().createTNFeInfNFeDetProd();
				det.setNItem(getInteger(String.valueOf(++nItem), 3, true));

				prod.setCProd(getInteger(rs.getString("CODPROD"), 60, true));
				String ean = getString(rs.getString("CODEANPROD"), 14, true);
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
				prod.setQCom(getDouble(rs.getString("QTDITCOMPRA"), 12, 4, true));
				prod.setVUnCom(getDouble(rs.getString("PRECOITCOMPRA"), 16, 4, true));
				prod.setVProd(getDouble(rs.getString("VLRPRODITCOMPRA"), 15, 2, true));
				
				prod.setUTrib(getString(rs.getString("CODUNID"), 6, true));
				prod.setQTrib(getDouble(rs.getString("QTDITCOMPRA"), 12, 4, true));
				prod.setVUnTrib(getDouble(rs.getString("PRECOITCOMPRA"), 16, 4, true));
				prod.setVFrete(getDouble(rs.getString("VLRFRETEITCOMPRA"), 15, 2, false, true));
				// Verificar se o valor do seguro é adicionado no CPITCOMPRA.VLRADICITCOMPRA
				prod.setVSeg(getDouble(null , 15, 2, false, true));
				prod.setVDesc(getDouble(rs.getString("VLRDESCITCOMPRA"), 15, 2, false, true));
				prod.setIndTot("1");
				if(di != null){
					prod.getDI().add(di);
				}
				
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
					/* TODO - ISS NÃO IMPLEMENTADO NA COMPRA DO FREEDOM
					TNFe.InfNFe.Det.Imposto.ISSQN issqn = new ObjectFactory().createTNFeInfNFeDetImpostoISSQN();
					issqn.setVBC(getDouble(rs.getString("VLRBASEICMSITCOMPRA"), 15, 2, true));
					issqn.setVAliq(getDouble(rs.getString("PERCISSITCOMPRA"), 5, 2, true));
					issqn.setVISSQN(getDouble(rs.getString("VLRISSITCOMPRA"), 15, 2, true));
					issqn.setCMunFG(getString(ide.getCMunFG(), 7, true));
					issqn.setCListServ(getString(rs.getString("CODSERV"), 4, true, "."));
					// TODO - 324a | U07 - Registro nacional de veiculos de carga. N=NORMAL; R=RETIDA; S=SUBSTITUTA; I=ISENTA
					// Não implementado no Freedom-erp - 4.01-NT2009.006x
					issqn.setCSitTrib(getString("N", 1, true));
					*/
				} else {
					
					prod.setNCM(NFeUtil.tratarNcm(getString(rs.getString("CODNCM"), 8, true, ".")));
					
					TNFe.InfNFe.Det.Imposto.ICMS icms = new ObjectFactory().createTNFeInfNFeDetImpostoICMS();
					impostos.setICMS(icms);
					
					if(this.simples && emit.getCRT().equals("1")){
						
						String csosn = getString(rs.getString("CSOSN"), 3);
						if(csosn != null){
							
							if (csosn.equals("101")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMSSN101 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMSSN101();
								icms.setICMSSN101(tipoIcms);
								tipoIcms.setCSOSN(csosn);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setPCredSN(getDouble(new Double(this.aliqSimples).toString(), 5, 2, true));
								BigDecimal vlrCred = new BigDecimal(prod.getVProd()).multiply(new BigDecimal(this.aliqSimples).divide(new BigDecimal(100)));
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
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITCOMPRA"), 5, 2));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPCredSN(getDouble(new Double(this.aliqSimples).toString(), 5, 2, true));
								BigDecimal vlrCred = new BigDecimal(prod.getVProd()).multiply(new BigDecimal(this.aliqSimples).divide(new BigDecimal(100)));
								tipoIcms.setVCredICMSSN(getDouble(vlrCred.toString(), 15, 2));
							} else if (csosn.equals("202") || csosn.equals("203")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMSSN202 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMSSN202();
								icms.setICMSSN202(tipoIcms);
								tipoIcms.setCSOSN(csosn);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITCOMPRA"), 5, 2));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITCOMPRA"), 15, 2, true));
							} else if (csosn.equals("500")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMSSN500 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMSSN500();
								icms.setICMSSN500(tipoIcms);
								tipoIcms.setCSOSN(csosn);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBCSTRet("0.00");
								tipoIcms.setVICMSSTRet("0.00");
							} else if (csosn.equals("900")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMSSN900 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMSSN900();
								icms.setICMSSN900(tipoIcms);
								tipoIcms.setCSOSN(csosn);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setPRedBC(getDouble(rs.getString("REDFISC"), 5, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITCOMPRA"), 5, 2));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPCredSN(getDouble(new Double(this.aliqSimples).toString(), 5, 2, true));
								BigDecimal vlrCred = new BigDecimal(prod.getVProd()).multiply(new BigDecimal(this.aliqSimples).divide(new BigDecimal(100)));
								tipoIcms.setVCredICMSSN(getDouble(vlrCred.toString(), 15, 2));
							}
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
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITCOMPRA"), 15, 2, true));
							} else if (cst.equals("10")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS10 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS10();
								icms.setICMS10(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITCOMPRA"), 5, 2));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
							} else if (cst.equals("20")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS20 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS20();
								icms.setICMS20(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setPRedBC(getDouble(rs.getString("REDFISC"), 5, 2, true));
							} else if (cst.equals("30")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS30 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS30();
								icms.setICMS30(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setModBCST(getInteger(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITCOMPRA"), 5, 2));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
							} else if (cst.equals("40") || cst.equals("41") || cst.equals("50")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS40 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS40();
								icms.setICMS40(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								/*
								 * Usado somente para veiculos beneficiados com a
								 * desoneração condicional do ICMS, e especificar o motiva
								 * da desoneração
								 * tipoIcms.setVICMS(rs.getString("VLRICMSITCOMPRA"));
								 * tipoIcms.setMotDesICMS(rs.getString(null));
								 */
							} else if (cst.equals("51")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS51 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS51();
								icms.setICMS51(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setPRedBC(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
							} else if (cst.equals("60")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS60 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS60();
								icms.setICMS60(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setOrig(getString(rs.getString("ORIGFISC")));
							} else if (cst.equals("70")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS70 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS70();
								icms.setICMS70(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITCOMPRA"), 5, 2));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPRedBC(getDouble(rs.getString("REDFISC"), 5, 2, true));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, true));
							} else if (cst.equals("90")) {
								TNFe.InfNFe.Det.Imposto.ICMS.ICMS90 tipoIcms = new ObjectFactory().createTNFeInfNFeDetImpostoICMSICMS90();
								icms.setICMS90(tipoIcms);
								tipoIcms.setCST(cst);
								tipoIcms.setModBC(getInteger(rs.getString("MODBCICMS"), 1, true));
								tipoIcms.setOrig(getInteger(rs.getString("ORIGFISC"), 1, true));
								tipoIcms.setVBC(getDouble(rs.getString("VLRBASEICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMS(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMS(getDouble(rs.getString("VLRICMSITCOMPRA"), 15, 2, true));
								tipoIcms.setModBCST(getString(rs.getString("MODBCICMSST"), 1, true));
								tipoIcms.setPMVAST(getDouble(rs.getString("MARGEMVLAGRITCOMPRA"), 5, 2));
								tipoIcms.setVBCST(getDouble(rs.getString("VLRBASEICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPICMSST(getDouble(rs.getString("PERCICMSITCOMPRA"), 5, 2, true));
								tipoIcms.setVICMSST(getDouble(rs.getString("VLRICMSSTITCOMPRA"), 15, 2, true));
								tipoIcms.setPRedBC(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
								tipoIcms.setPRedBCST(getDouble(rs.getString("REDFISC"), 5, 2, false, true));
							}
						}
					}
					
					String cstIpi = getString(rs.getString("CODSITTRIBIPI"), 2);
					if(cstIpi != null){
						TNFe.InfNFe.Det.Imposto.IPI ipi = new ObjectFactory().createTNFeInfNFeDetImpostoIPI();
						// TODO - 251 - O06 - cEnq - Cód. enquadramento legal do ipi. Tabela a ser criada pela RFB, informar 999 enquanto a tabela não for criada
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
								ipiTrib.setPIPI(getDouble(rs.getString("PERCIPIITCOMPRA"), 5, 2, true));
								ipiTrib.setVBC(getDouble(rs.getString("VLRBASEIPIITCOMPRA"), 15, 2, true));
							} else if (rs.getString("TPCALCIPI").equals("V")) {
								ipiTrib.setQUnid(getDouble(rs.getString("QTDITCOMPRA"),16, 4, true));
								ipiTrib.setVUnid(getDouble(rs.getString("VLRIPIUNIDTRIB"), 15, 2, true));
							}
							ipiTrib.setVIPI(getDouble(rs.getString("VLRIPIITCOMPRA"),15, 2, true));
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
							pisAliq.setVBC(getDouble(rs.getString("VLRPRODITCOMPRA"), 15, 2, true));
							pisAliq.setPPIS(getDouble(rs.getString("ALIQPISFISC"), 5, 2, true));
							pisAliq.setVPIS(getDouble(String.valueOf((rs.getDouble("VLRPRODITCOMPRA") * rs.getDouble("ALIQPISFISC")) / 100), 15, 2, true));
						} else if (cstPis.matches("^(03)")) {
							TNFe.InfNFe.Det.Imposto.PIS.PISQtde pisQtde = new ObjectFactory().createTNFeInfNFeDetImpostoPISPISQtde();
							pis.setPISQtde(pisQtde);
							pisQtde.setCST(cstPis);
							pisQtde.setQBCProd(getDouble(rs.getString("QTDITCOMPRA"), 16, 4, true));
							pisQtde.setVAliqProd(getDouble(rs.getString("VLRPISUNIDTRIB"), 15, 4, true));
							pisQtde.setVPIS(getDouble(String.valueOf(rs.getDouble("VLRPISUNIDTRIB") * rs.getDouble("QTDITCOMPRA")), 15, 2, true));
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
							pisOutr.setVBC(getDouble(rs.getString("VLRPRODITCOMPRA"), 15, 2, true));
							pisOutr.setPPIS(getDouble(rs.getString("ALIQPISFISC"), 5, 2, true));
							pisOutr.setVPIS(getDouble(String.valueOf((rs.getDouble("VLRPRODITCOMPRA") * rs.getDouble("ALIQPISFISC")) / 100), 15, 2, true));
							// }else if(rs.getString("TPCALCPIS").equals("V")){
							// pisOutr.setQBCProd(rs.getString("QTDITCOMPRA"));
							// pisOutr.setVAliqProd(rs.getString("VLRPISUNIDTRIB"));
							// pisOutr.setVPIS(String.valueOf(rs.getDouble("VLRPISUNIDTRIB")
							// * rs.getDouble("QTDITCOMPRA")));
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
							cofinsAliq.setVBC(getDouble(rs.getString("VLRPRODITCOMPRA"), 15, 2, true));
							cofinsAliq.setPCOFINS(getDouble(rs.getString("ALIQCOFINSFISC"), 5, 2, true));
							cofinsAliq.setVCOFINS(getDouble(String.valueOf((rs.getDouble("VLRPRODITCOMPRA") * rs.getDouble("ALIQCOFINSFISC")) / 100), 15, 2, true));
						} else if (cstCof.matches("^(03)")) {
							TNFe.InfNFe.Det.Imposto.COFINS.COFINSQtde cofinsQtde = new ObjectFactory().createTNFeInfNFeDetImpostoCOFINSCOFINSQtde();
							cofins.setCOFINSQtde(cofinsQtde);
							cofinsQtde.setCST(cstCof);
							cofinsQtde.setQBCProd(getDouble(rs.getString("QTDITCOMPRA"), 16, 4, true));
							cofinsQtde.setVAliqProd(getDouble(rs.getString("VLRCOFUNIDTRIB"), 15, 4, true));
							cofinsQtde.setVCOFINS(getDouble(String.valueOf(rs.getDouble("VLRCOFUNIDTRIB") * rs.getDouble("QTDITCOMPRA")), 15, 2, true));
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
							cofinsOutr.setVBC(getDouble(rs.getString("VLRPRODITCOMPRA"), 15, 2, true));
							cofinsOutr.setPCOFINS(getDouble(rs.getString("ALIQCOFINSFISC"), 5, 2, true));
							cofinsOutr.setVCOFINS(getDouble(String.valueOf((rs.getDouble("VLRPRODITCOMPRA") * rs.getDouble("ALIQCOFINSFISC")) / 100), 15, 2, true));
							// }else if(rs.getString("TPCALCCOF").equals("V")){
							// cofinsOutr.setQBCProd(rs.getString("QTDITCOMPRA"));
							// cofinsOutr.setVAliqProd(rs.getString("VLRCOFUNIDTRIB"));
							// cofinsOutr.setVCOFINS(String.valueOf(rs.getDouble("VLRPISUNIDTRIB")
							// * rs.getDouble("QTDITCOMPRA")));
							// }
						}
						impostos.setCOFINS(cofins);
					}
					
					// TODO - Cofins Substituição Tributária
					// TNFe.InfNFe.Det.Imposto.COFINSST cofinsST = new
					// ObjectFactory().createTNFeInfNFeDetImpostoCOFINSST();
					// impostos.setCOFINSST(cofinsST);
					
				}
				
				//if(rs.getString("CODMENS") != null){
				//	det.setInfAdProd(getString(NFeUtil.geraMensagens(mens1, vlricmssimples, percicmssimples), 500));
				//}
				det.setProd(prod);
				det.setImposto(impostos);
				infNFe.getDet().add(det);
			}
			
			conSys.commit();
			
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null, "Erro ao carregar itens da compra!\n" + err.getMessage(), true, conSys, err);
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
			+ "fre.TIPOFRETECP, fre.CONHECFRETECP, fre.PLACAFRETECP, fre.UFFRETECP, fre.QTDFRETECP, fre.PESOBRUTCP, fre.PESOLIQCP, fre.ESPFRETECP, fre.MARCAFRETECP, cfre.VLRFRETE, cfre.ALIQICMSFRETE, cfre.VLRICMSFRETE, cfre.VLRBASEICMSFRETE, cfre.CODNAT "
			+ "FROM CPFRETECP fre LEFT JOIN VDTRANSP tra ON (fre.CODTRAN = tra.CODTRAN) LEFT JOIN LFFRETE cfre ON (fre.CONHECFRETECP = cfre.CODFRETE) LEFT JOIN SGUF uf ON (tra.SIGLAUF = uf.SIGLAUF) "
			+ "WHERE fre.CODEMP=? AND fre.CODFILIAL=? AND fre.CODCOMPRA=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODCOMPRA));
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
				veicTransp.setPlaca(getString(rs.getString("PLACAFRETEVD").replace("*", "0"), 8, true));
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
		String sql = "SELECT PG.CODPAG, PG.VLRPAG, PG.VLRDESCPAG, PG.VLRAPAGPAG "
				+ "FROM FNPAGAR PG WHERE CODEMP=? AND CODFILIAL=? AND CODCOMPRA=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODCOMPRA));
			rs = ps.executeQuery();

			if (rs.next()) {
				TNFe.InfNFe.Cobr cobr = new ObjectFactory()
						.createTNFeInfNFeCobr();
				infNFe.setCobr(cobr);
				TNFe.InfNFe.Cobr.Fat fat = new ObjectFactory()
						.createTNFeInfNFeCobrFat();
				cobr.setFat(fat);
				fat.setNFat(getString(rs.getString("CODPAG"), 60));
				fat.setVOrig(getDouble(rs.getString("VLRPAG"), 15, 2, false, true));
				fat.setVDesc(getDouble(rs.getString("VLRDESCPAG"), 15, 2, false, true));
				fat.setVLiq(getDouble(rs.getString("VLRAPAGPAG"), 15, 2, false, true));
				int codPag = rs.getInt("CODPAG");
				
				conSys.commit();

				sql = "SELECT ITP.NPARCPAG, ITP.DTVENCITPAG, ITP.VLRPARCITPAG " 
					+ "FROM FNITPAGAR ITP WHERE ITP.CODEMP=? AND ITP.CODFILIAL=? AND ITP.CODPAG=?";
				ps = conSys.prepareStatement(sql);
				ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
				ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
				ps.setInt(3, codPag);
				rs = ps.executeQuery();
				while (rs.next()) {
					TNFe.InfNFe.Cobr.Dup dup = new ObjectFactory().createTNFeInfNFeCobrDup();
					dup.setNDup(getString(rs.getString("NPARCPAG"), 60));
					dup.setDVenc(getDate(rs.getString("DTVENCITPAG")));
					dup.setVDup(getDouble(rs.getString("VLRPARCITPAG"), 15, 2, false, true));
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

			sql = "UPDATE CPCOMPRA SET CHAVENFECOMPRA = ? WHERE CODEMP=? AND CODFILIAL=? AND CODCOMPRA=?";
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
				ps.setInt(4, (Integer) key.get(FreedomNFEKey.CODCOMPRA));
				ps.execute();
				conSys.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		sql = "EXECUTE PROCEDURE CPADICNFEOPERASP(?,?,?,?,?)";
		try {
			ps = conNFE.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODCOMPRA));
			if(chaveNfe != null && chaveNfe.trim().length() > 0){
				ps.setString(4, chaveNfe);
			}else{
				ps.setNull(4, Types.CHAR);
			}
			if(pathXML != null && pathXML.trim().length() > 0){
				ps.setString(5, pathXML);
			}else{
				ps.setNull(5, Types.CHAR);
			}
			ps.execute();
			
			conNFE.commit();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
