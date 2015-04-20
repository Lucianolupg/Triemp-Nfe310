package br.com.triemp.nfe.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.freedom.library.functions.Funcoes;

public class NFeUtil {

	/**
	 * Gera o dígito da NF-e através da chave de acesso.
	 * 
	 * @param chave
	 * @return Dígito
	 */
	public static String getDvChaveNFe(String chave) {
		String peso = "4329876543298765432987654329876543298765432";
		int soma = 0;
		int dv = 0;
		try {
			for(int i = 0; i < chave.length(); i++){
				soma += Integer.parseInt(chave.substring(i, i + 1))	* Integer.parseInt(peso.substring(i, i + 1));
			}
			dv = 11 - (soma % 11);
			if(dv > 9){
				dv = 0;
			}
		} catch (Exception err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao gerar dígito da chave da NF-e!\n" + err.getMessage(), true, null, err);
		}
		return String.valueOf(dv);
	}
	
	/**
	 * Gera o dígito do Cód. Município através do código informado "UUNNNN" onde UU = Cód. UF do IBGE e NNNN = Cód. de ordem dentro da UF.
	 * 
	 * @param uunnnn
	 * @return Dígito
	 */
	public static String getDvCodMunic(String uunnnn){
		String peso = "121212";
		int soma = 0;
		int dv = 0;
		try {
			for(int i = 0; i < uunnnn.length(); i++){
				int aux = Integer.parseInt(uunnnn.substring(i, i + 1)) * Integer.parseInt(peso.substring(i, i + 1));
				if(aux > 9){
					aux = Integer.parseInt(String.valueOf(aux).substring(0, 1)) + Integer.parseInt(String.valueOf(aux).substring(1, 2));
				}
				soma += aux;
			}
			int resto = soma % 10;
			if(resto != 0){
				dv = 10 - resto;
			}else{
				dv = 0;
			}
		} catch (Exception err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao gerar dígido do código do município!\n" + err.getMessage(), true, null, err);
		}
		return String.valueOf(dv);
	}
	
	/**
	 * Gera o dígito do Cód. Pais através do código informado "NNN" onde NNN = Númerode ordem do cód. do País.
	 * 
	 * @param nnn
	 * @return Dígito
	 */
	public static String getDvCodPais(String nnn){
		String peso = "432";
		int soma = 0;
		int dv = 0;
		try {
			for(int i = 0; i < nnn.length(); i++){
				soma += Integer.parseInt(nnn.substring(i, i + 1)) * Integer.parseInt(peso.substring(i, i + 1));
			}
			int resto = soma % 11;
			if(resto != 0 && resto != 1){
				dv = 11 - resto;
			}else{
				dv = 0;
			}
		} catch (Exception err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao gerar dígido do código do país!\n" + err.getMessage(), true, null, err);
		}
		return String.valueOf(dv);
	}
	
	public static String lpad(String valueToPad, String filler, int size) {
		while (valueToPad.length() < size) {
			valueToPad = filler + valueToPad;
		}
		return valueToPad;
	}
	
	public static String rpad(String valueToPad, String filler, int size) {
		while (valueToPad.length() < size) {
			valueToPad = valueToPad+filler;
		}
		return valueToPad;
	}
	
	public static boolean isValidBarCodeEAN(String barCode) {
        int digit;
        int calculated;
        String ean;
        String checkSum = "131313131313";
        int sum = 0;

        if (barCode.length() == 8 || barCode.length() == 13) {
            digit = Integer.parseInt("" + barCode.charAt(barCode.length() - 1)); 
            ean = barCode.substring(0, barCode.length() - 1);            
            for (int i = 0; i <= ean.length() - 1; i++) {
            	if ( Character.isDigit( ean.charAt(i) ) ) {
            		sum += (Integer.parseInt("" + ean.charAt(i))) * (Integer.parseInt("" + checkSum.charAt(i)));
            	}
            	else {
            		return false;
            	}
            }            
            calculated = 10 - (sum % 10);
            return (digit == calculated);
        } else {
            return false;
        }
    }
	
	public static String tratarNcm(String ncm){
		if(ncm.length() > 0){
			while(ncm.substring(0, 1).equals("0")){
				ncm = ncm.substring(1, ncm.length()) + "0";
			}
		}
		return ncm;
	}
	
	/**
	 * Método para tratamento de mensagem para crédito de ICMS de Empresa SIMPLES
	 * 
	 *  @param mensagem, valor icms simples, perc. icms simples
	 *  @return mensagem
	 */
	public static String geraMensagens(String mens1, String vlricmssimples, String percicmssimples) {
		if( (vlricmssimples!=null) && (percicmssimples!=null)) {								
			if(mens1!=null) {
				mens1 = mens1.replaceAll( "#VALOR#", vlricmssimples);
				mens1 = mens1.replaceAll( "#ALIQUOTA#", percicmssimples+ "% " );
			}
		}
		return mens1;
	}
	
	public static boolean copy(File orig, File dest){
		try {
			FileInputStream fileInputStream = new FileInputStream(orig);
			FileOutputStream fileOutputStream = new FileOutputStream(dest);
			FileChannel fcOrig = fileInputStream.getChannel();
			FileChannel fcDest = fileOutputStream.getChannel();
			fcOrig.transferTo(0, fcOrig.size(), fcDest);
			fileInputStream.close();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		if(orig.getUsableSpace() == dest.getUsableSpace()){
			return true;
		}else{
			return false;
		}
	}
	
	public static String criaDiretorio(String dir){
		File d = new File(dir);
		if(!d.exists() || !d.isDirectory()){
			d.mkdirs();
		}
		return dir;
	}
}