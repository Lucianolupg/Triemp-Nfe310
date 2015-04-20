/**
 * @version 14/07/2003 <BR>
 * @author Setpoint Informática Ltda./Fernando Oliveira da Silva <BR>
 * 
 *         Projeto: Freedom <BR>
 * 
 *         Pacote: org.freedom.modulos.std <BR>
 *         Classe: @(#)DLConsultaPgto.java <BR>
 * 
 *         Este arquivo é parte do sistema Freedom-ERP, o Freedom-ERP é um software livre; você pode redistribui-lo e/ou <BR>
 *         modifica-lo dentro dos termos da Licença Pública Geral GNU como publicada pela Fundação do Software Livre (FSF); <BR>
 *         na versão 2 da Licença, ou (na sua opnião) qualquer versão. <BR>
 *         Este programa é distribuido na esperança que possa ser util, mas SEM NENHUMA GARANTIA; <BR>
 *         sem uma garantia implicita de ADEQUAÇÂO a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. <BR>
 *         Veja a Licença Pública Geral GNU para maiores detalhes. <BR>
 *         Você deve ter recebido uma cópia da Licença Pública Geral GNU junto com este programa, se não, <BR>
 *         escreva para a Fundação do Software Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA <BR>
 * <BR>
 * 
 *         Comentários sobre a classe...
 */

package br.com.triemp.nfe.dialog;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.freedom.bmps.Icone;
import org.freedom.infra.pojos.Constant;
import org.freedom.library.functions.Funcoes;
import org.freedom.library.swing.component.JButtonPad;
import org.freedom.library.swing.component.JPanelPad;
import org.freedom.library.swing.component.JTablePad;
import org.freedom.library.swing.dialog.FFDialogo;
import org.freedom.library.swing.frame.Aplicativo;
import org.freedom.modules.nfe.control.AbstractNFEFactory;

import br.com.triemp.modules.nfe200.control.NFe;
import br.com.triemp.nfe.client.NFeClientACBr;
import br.com.triemp.nfe.util.NFeUtil;

public class DLConsultaNFe extends FFDialogo {

	private static final long serialVersionUID = 1L;
	
	private JTablePad tabStatus = new JTablePad();

	private JScrollPane spnTab = new JScrollPane( tabStatus );
	
	private JButtonPad btValidar = new JButtonPad(Icone.novo("btExecuta.gif"));
	
	private JButtonPad btEnviar = new JButtonPad(Icone.novo("btOk.gif"));
	
	private JButtonPad btStatus = new JButtonPad(Icone.novo("btReset.gif"));
	
	private JButtonPad btExcluir = new JButtonPad(Icone.novo("btExcluir.gif"));
	
	private JButtonPad btPrevimp = new JButtonPad(Icone.novo("btPrevimp.gif"));
	
	private JButtonPad btEnviarMail = new JButtonPad(Icone.novo("btEnviarMail.gif"));
	
	private JButtonPad btDigitaMail = new JButtonPad(Icone.novo("btEmail.gif"));
	
	private JButtonPad btCancelar = new JButtonPad(Icone.novo("btCancelar.gif"));
	
	private NFe nfe;
	
	private String separador;
	
	private String pathAtual;
	
	private String fileAcbr;
	
	private String chaveNFe;
	
	private Constant tipoNF;
	
	private NFeClientACBr nfeClient;
	
	private HashMap<String, String> retorno;
	
	public DLConsultaNFe( NFe nfe, Constant tipoNF ) {
		
		setTitulo( "Consulta Nota Fiscal Eletrônica" );
		setAtribos( 200, 200, 500, 300 );
		
		setToFrameLayout();
		
		pnGrid = new JPanelPad(JPanelPad.TP_JPANEL, new GridLayout(1, 1));
		
		pnGrid.add(btValidar);
		pnGrid.add(btEnviar);
		pnGrid.add(btStatus);
		pnGrid.add(btExcluir);
		pnGrid.add(btPrevimp);
		pnGrid.add(btEnviarMail);
		pnGrid.add(btDigitaMail);
		pnGrid.add(btCancelar);
		
		btValidar.setToolTipText( "Validar arquivo XML (F2)" );
		btEnviar.setToolTipText( "Enviar NFe (F3)" );
		btStatus.setToolTipText( "Recarregar Status da NFe (F4)" );
		btExcluir.setToolTipText( "Excluir XML e informações da NFe (F5)" );
		btPrevimp.setToolTipText( "Imprimir NFe (F6)" );
		btEnviarMail.setToolTipText( "Enviar e-mail da NFe (F7)" );
		btDigitaMail.setToolTipText( "Digitar e-mail da NFe (F8)" );
		btCancelar.setToolTipText( "Cancelar NFe (F9)" );
		
		btValidar.addActionListener(this);
		btEnviar.addActionListener(this);
		btStatus.addActionListener(this);
		btExcluir.addActionListener(this);
		btPrevimp.addActionListener( this );
		btEnviarMail.addActionListener( this );
		btDigitaMail.addActionListener( this );
		btCancelar.addActionListener( this );
		
		btValidar.addKeyListener(this);
		btEnviar.addKeyListener(this);
		btStatus.addKeyListener(this);
		btExcluir.addKeyListener(this);
		btPrevimp.addKeyListener( this );
		btEnviarMail.addKeyListener( this );
		btDigitaMail.addKeyListener( this );
		btCancelar.addKeyListener( this );
		
		pnRodape.add(pnGrid);
		
		tabStatus.setFocusable(false);
		spnTab.setFocusable(false);
		
		c.add( spnTab );

		tabStatus.adicColuna("Chave");
		tabStatus.adicColuna("Valor");
		tabStatus.setTamColuna(100, 0);
		tabStatus.setTamColuna(600, 1);
		
		this.nfe = nfe;
		this.tipoNF = tipoNF;
		separador = nfe.getSeparador();
		pathAtual = nfe.getPathAtual();
		fileAcbr = Aplicativo.getParameter("pathnfe") + separador + pathAtual;
		
		nfeClient = new	NFeClientACBr(Aplicativo.getParameter("ipservnfe"), Integer.valueOf(Aplicativo.getParameter("portservnfe")));
		if(!nfeClient.conectar()){
			nfeClient.close();
		}
		
		assinarNFe();
		carregaStatusNFe();

	}
	
	private void assinarNFe() {
		if(!nfeClient.isClose()){
			fileAcbr = Aplicativo.getParameter("pathnfe") + separador + pathAtual;
			nfeClient.assinarNFe(fileAcbr);
		}
	}

	private void carregaStatusNFe() {
		if(!nfeClient.isClose()){
			fileAcbr = Aplicativo.getParameter("pathnfe") + separador + pathAtual;
			retorno = nfeClient.consultarNFe(fileAcbr);
			carregaStatusNFe(retorno);
		}
	}
	
	private void carregaStatusNFe(HashMap<String, String> retorno){
		limpaTabela();
		String CStat = "";
		if(retorno != null && retorno.size() > 0){
			Iterator<?> it = retorno.entrySet().iterator();
			while(it.hasNext()){
				Entry key = (Entry)it.next();
				tabStatus.adicLinha(new Object[] { key.getKey(), key.getValue()});
			}
			CStat = retorno.get("CStat");
		}
		
		// 100 - Autorizado o uso da NFe
		if("100".equals(CStat)){
			moveEnviado(retorno);
			btValidar.setEnabled(false);
			btEnviar.setEnabled(false);
			btStatus.setEnabled(true);
			btExcluir.setEnabled(false);
			btPrevimp.setEnabled(true);
			btEnviarMail.setEnabled(true);
			btDigitaMail.setEnabled(true);
			btCancelar.setEnabled(true);
		}// 101 - Cancelamento de NFe homologado ou 105 - Lote em processament
		else if("101".equals(CStat) || "105".equals(CStat)){
			btValidar.setEnabled(false);
			btEnviar.setEnabled(false);
			btStatus.setEnabled(true);
			btExcluir.setEnabled(false);
			btPrevimp.setEnabled(false);
			btEnviarMail.setEnabled(false);
			btDigitaMail.setEnabled(false);
			btCancelar.setEnabled(false);
		}// 217 Rejeição: NFe não consta na base de dados da SEFAZ
		else if("217".equals(CStat)){
			btValidar.setEnabled(true);
			btEnviar.setEnabled(false);
			btStatus.setEnabled(false);
			btExcluir.setEnabled(true);
			btPrevimp.setEnabled(false);
			btEnviarMail.setEnabled(false);
			btDigitaMail.setEnabled(false);
			btCancelar.setEnabled(false);
			tabStatus.adicLinha(new Object[] { "Dica", "Clique no botão 'Validar arquivo XML' ou tecla F2"});
		}else{
			btValidar.setEnabled(false);
			btEnviar.setEnabled(false);
			btStatus.setEnabled(true);
			btExcluir.setEnabled(true);
			btPrevimp.setEnabled(false);
			btEnviarMail.setEnabled(false);
			btDigitaMail.setEnabled(false);
			btCancelar.setEnabled(false);
		}
		chaveNFe = retorno.get("ChNFe");
	}
	
	private void validarNFe(){
		if(!nfeClient.isClose()){
			
			Date dateEmi = null;
			Date dateNow = null;
			try {
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				dateEmi = (Date) formatter.parse(nfe.getNfe().getInfNFe().getIde().getDEmi());
				dateNow = (Date) formatter.parse(formatter.format(new Date()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if(dateEmi.before(dateNow)){
				if(Funcoes.mensagemConfirma(null, 
						"CUIDADO!\nData de emissão da nota é anterior a data atual.\nDeseja emitir a nota eletrônica mesmo assim?") 
						== JOptionPane.NO_OPTION){
					this.setVisible(false);
					return;
				}
			}
			
			fileAcbr = Aplicativo.getParameter("pathnfe") + separador + pathAtual;
			String ret = nfeClient.validarNFe(fileAcbr);
			if(ret.indexOf("OK") == -1){
				JOptionPane.showMessageDialog(this, ret, "Validar XML", JOptionPane.ERROR_MESSAGE);
			}else{
				btEnviar.setEnabled(true);
				JOptionPane.showMessageDialog(this, "XML validado com sucesso", "Validar XML", JOptionPane.INFORMATION_MESSAGE);
				tabStatus.adicLinha(new Object[] { "Dica", "Clique no botão 'Enviar NFe' ou tecla F3"});
			}
		}
	}
	
	private void imprimirNFe(){
		if(!nfeClient.isClose()){
			fileAcbr = Aplicativo.getParameter("pathnfe") + separador + pathAtual;
			nfeClient.imprimirDanfe(fileAcbr);
		}
	}
	
	private void enviarEmailNFe(boolean digita){
		if(!nfeClient.isClose()){
			String ret;
			String email;
			Object[] emails = nfe.getEmailsNfe().toArray();
			if(digita || emails.length == 0){
				email = JOptionPane.showInputDialog(this,"Informe o e-mail do destinatario da NFe.");
			}else{
				email = (String) JOptionPane.showInputDialog(this,"Informe o e-mail do destinatario da NFe.", "Enviar e-mail da NF-e", JOptionPane.QUESTION_MESSAGE, null, emails, emails[0]);
			}
			if(email != null){
				if(email.length() > 0){
					email = email.substring(email.indexOf("]")+1).replace(" ", "");
					fileAcbr = Aplicativo.getParameter("pathnfe") + separador + pathAtual;
					ret = nfeClient.enviarEmail(email, fileAcbr, true);
					if(ret.indexOf("OK") != -1){
						JOptionPane.showMessageDialog(this, "E-mail enviado com sucesso", "Enviar e-mail", JOptionPane.INFORMATION_MESSAGE);
					}else{
						JOptionPane.showMessageDialog(this, ret, "Enviar e-mail", JOptionPane.ERROR_MESSAGE);
					}
				}else{
					JOptionPane.showMessageDialog(this, "E-mail não informado", "Enviar e-mail", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private void enviarNFe(){
		if(!nfeClient.isClose()){
			
			NFeUtil.criaDiretorio(nfe.getPathFreedom() + separador + "temp");
			replacePathAtual("temp");
			File xmlTemp = new File(nfe.getPathFreedom() + separador + pathAtual);
			
			if(NFeUtil.copy(nfe.getXmlNFe(), xmlTemp)){
				nfe.setPathAtual(pathAtual);
				nfe.getXmlNFe().delete();
				nfe.setXmlNFe(xmlTemp);
				
				fileAcbr = Aplicativo.getParameter("pathnfe") + separador + pathAtual;	
				retorno = nfeClient.enviarNFe(fileAcbr, nfe.getNfe().getInfNFe().getIde().getNNF(), true, false);
				
				carregaStatusNFe(retorno);
			}
		}
	}

	private void cancelarNFe(){
		if(!nfeClient.isClose()){
			if(chaveNFe != null && chaveNFe.length() > 0){
				String just = JOptionPane.showInputDialog(this,"Informe uma justificativa para o cancelamento da NFe (Min. 15 dig.)", "Cancelar NFe",JOptionPane.QUESTION_MESSAGE);
				if(just != null){
					if(just.length() > 14){
						retorno = nfeClient.cancelarNFe(chaveNFe, just);
						if("101".equals(retorno.get("CStat"))){
							pathAtual = pathAtual.replace("-nfe", "-nfe-cancelada");
							File xmlCancelado = new File(nfe.getPathFreedom() + separador + pathAtual);
							nfe.getXmlNFe().renameTo(xmlCancelado);
							nfe.setXmlNFe(xmlCancelado);
							nfe.setPathAtual(pathAtual);
							btStatus.doClick();
						}else{
							carregaStatusNFe(retorno);
						}
					}else{
						JOptionPane.showMessageDialog(this, "Justificativa não informado ou menor que 15 digitos", "Cancelamento de NFe", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}
	
	private void excluirNFe(){
		if(Funcoes.mensagemConfirma(null, "Deseja excluir o aquivo XML e todos os registro referente a essa NFe?") == JOptionPane.YES_OPTION){
			NFeUtil.criaDiretorio(nfe.getPathFreedom() + separador + "deletado");
			replacePathAtual("deletado");
			File xmlDelet = new File(nfe.getPathFreedom() + separador + pathAtual);
			if(NFeUtil.copy(nfe.getXmlNFe(), xmlDelet)){
				nfe.getXmlNFe().delete();
				nfe.setXmlNFe(null);
			}
			nfe.setChaveNfe("");
			nfe.setPathAtual("");
			
			JOptionPane.showMessageDialog(this, "Arquivo XML excluido com sucesso", "Excluir XML NFe", JOptionPane.INFORMATION_MESSAGE);
			this.setVisible(false);
		}
	}
	
	public void actionPerformed( ActionEvent evt ) {
		if ( evt.getSource() == btValidar ) {
			validarNFe();
		}else if ( evt.getSource() == btEnviar ) {
			enviarNFe();
		}else if ( evt.getSource() == btStatus ) {
			carregaStatusNFe();
		}else if ( evt.getSource() == btExcluir ) {
			excluirNFe();
		}else if ( evt.getSource() == btPrevimp ) {
			imprimirNFe();
		}else if ( evt.getSource() == btEnviarMail ) {
			enviarEmailNFe(false);
		}else if ( evt.getSource() == btDigitaMail ) {
			enviarEmailNFe(true);
		}else if ( evt.getSource() == btCancelar ) {
			cancelarNFe();
		}
	}
	
	public void keyPressed(KeyEvent kevt) {
		if ( kevt.getKeyCode() == KeyEvent.VK_F2 ) {
			btValidar.doClick();
		}else if ( kevt.getKeyCode() == KeyEvent.VK_F3 ) {
			btEnviar.doClick();
		}else if ( kevt.getKeyCode() == KeyEvent.VK_F4 ) {
			btStatus.doClick();
		}else if ( kevt.getKeyCode() == KeyEvent.VK_F5 ) {
			btExcluir.doClick();
		}else if ( kevt.getKeyCode() == KeyEvent.VK_F6 ) {
			btPrevimp.doClick();
		}else if ( kevt.getKeyCode() == KeyEvent.VK_F7 ) {
			btEnviarMail.doClick();
		}else if ( kevt.getKeyCode() == KeyEvent.VK_F8 ) {
			btDigitaMail.doClick();
		}else if ( kevt.getKeyCode() == KeyEvent.VK_F9 ) {
			btCancelar.doClick();
		}else if ( kevt.getKeyCode() == KeyEvent.VK_ESCAPE ) {
			setVisible(false);
		}
		
		super.keyPressed(kevt);
	}
	
	public String replacePathAtual(String newPath){

		if(pathAtual.indexOf("enviar") != -1){
			pathAtual = pathAtual.replace("enviar", newPath);
		}else if(pathAtual.indexOf("temp") != -1){
			pathAtual = pathAtual.replace("temp", newPath);
		}else if(pathAtual.indexOf("rejeitado") != -1){
			pathAtual = pathAtual.replace("rejeitado", newPath);
		}else if(pathAtual.indexOf("deletado") != -1){
			pathAtual = pathAtual.replace("deletado", newPath);
		}else if(pathAtual.indexOf("enviado") != -1){
			String path[] = pathAtual.split(separador);
			pathAtual = newPath + separador + path[path.length -1];
		}
		return pathAtual;
	}
	
	private void moveEnviado(HashMap<String, String> retorno) {
		if("100".equals(retorno.get("CStat")) && pathAtual.indexOf("enviado") == -1){
			
			Date date = null;
			GregorianCalendar data = new GregorianCalendar();
			
			try {
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				date = (Date) formatter.parse(nfe.getNfe().getInfNFe().getIde().getDEmi());
				data.setTime(date);
			} catch (ParseException e) {
				e.printStackTrace();
			} 
			
			String pathData = String.valueOf(data.get(Calendar.YEAR)) + separador + String.valueOf(data.get(Calendar.MONTH)+1);
			
			String pathEnviado = "";
			if(tipoNF.equals(AbstractNFEFactory.TP_NF_IN)){
				pathEnviado = "enviado" + separador + pathData + separador + "entrada";
			}else if(tipoNF.equals(AbstractNFEFactory.TP_NF_OUT)){
				pathEnviado = "enviado" + separador + pathData + separador + "saida";
			}
			
			NFeUtil.criaDiretorio(nfe.getPathFreedom() + separador + pathEnviado);
			replacePathAtual(pathEnviado);
			File xmlEnviado = new File(nfe.getPathFreedom() + separador + pathAtual);
			
			if(NFeUtil.copy(nfe.getXmlNFe(), xmlEnviado)){
				nfe.getXmlNFe().delete();
				nfe.setXmlNFe(xmlEnviado);
			}
			
			nfe.setPathAtual(pathAtual);
		}
	}
	
	private void limpaTabela(){
		int numLin = tabStatus.getNumLinhas();
		for(int i=0; i < numLin; i++) {
			tabStatus.tiraLinha(0);
		}
	}
	
	public void setVisible(boolean b) {
		if(b == false && !nfeClient.isClose()){
			nfeClient.close();
		}
		super.setVisible(b);
	}
}
