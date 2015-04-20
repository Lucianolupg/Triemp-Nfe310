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
				if(Funcoes.mensagemConfirma(null, "Não foi possível carregar o arquivo XML \"" + nfe.getPathAtual() + "\"." + 
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
