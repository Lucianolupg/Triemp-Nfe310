/**
 * @version 25/05/2010 
 * @author Triemp Solutions / Paulo Bueno <BR>
 * 
 * Projeto: Triemp-nfe <BR>
 * 
 * Pacote: br.com.triemp.modules.nfe <BR>
 * Classe: @(#)TriempNFEFactory.java <BR>
 * 
 * Este arquivo � parte do sistema Freedom-ERP, o Freedom-ERP � um software livre; voc� pode redistribui-lo e/ou <BR>
 * modifica-lo dentro dos termos da Licen�a P�blica Geral GNU como publicada pela Funda��o do Software Livre (FSF); <BR>
 * na vers�o 2 da Licen�a, ou (na sua opni�o) qualquer vers�o. <BR>
 * Este programa � distribuido na esperan�a que possa ser  util, mas SEM NENHUMA GARANTIA; <BR>
 * sem uma garantia implicita de ADEQUA��O a qualquer MERCADO ou APLICA��O EM PARTICULAR. <BR>
 * Veja a Licen�a P�blica Geral GNU para maiores detalhes. <BR>
 * Voc� deve ter recebido uma c�pia da Licen�a P�blica Geral GNU junto com este programa, se n�o, <BR>
 * de acordo com os termos da LPG-PC <BR>
 * <BR>
 */
package br.com.triemp.modules.nfe200.control;

import javax.swing.JOptionPane;

import org.freedom.library.functions.Funcoes;
import org.freedom.modules.nfe.control.AbstractNFEFactory;

import br.com.triemp.nfe.dialog.DLConsultaNFe;

public class TriempNFe extends AbstractNFEFactory {
	
	private NFe nfe;
	
	@Override
	public void post() {
		
		if(getTpNF().equals(AbstractNFEFactory.TP_NF_IN)){
			
			nfe = new NFeCompra(this.getConSys(), this.getConNFE(), this.getKey());
			
		}else if(getTpNF().equals(AbstractNFEFactory.TP_NF_OUT)){
			
			nfe = new NFeVenda(this.getConSys(), this.getConNFE(), this.getKey());
			
		}

		if(!nfe.getXmlNFe().exists()){
			if(nfe.getPathAtual() != null){
				if(Funcoes.mensagemConfirma(null, "N�o foi poss�vel carregar o arquivo XML \"" + nfe.getPathAtual() + "\"." + 
						"\n\nDeseja gerar um novo arquivo XML?" + 
						"\nDocumento: " + nfe.getNfe().getInfNFe().getIde().getNNF() + "\nChave: " + nfe.getNfe().getInfNFe().getId()) == JOptionPane.NO_OPTION){
					return;
				} 
			}
			
			nfe.gerarNFe();
		}
		
		DLConsultaNFe dlConsultaNFe = new DLConsultaNFe(nfe, getTpNF());
		dlConsultaNFe.setVisible(true);
	}

	@Override
	protected void runSend() {}

	@Override
	protected void validSend() {}

	public NFe getNfe() {
		return nfe;
	}

	@Override
	public boolean consistChaveNFE(String chavenfe) {
		// TODO Auto-generated method stub
		return false;
	}
}
