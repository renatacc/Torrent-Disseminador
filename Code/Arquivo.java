import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Arquivo {
	
	private String nome;
    //private byte[] conteudo;
    private int tamanhoByte;
	private int qtdPecas;
	private String local;
	private List<Integer> listaPecas;
	private Map<Integer, Integer> mapQtdPecasEnviadas;
	private RandomAccessFile arquivoFisico;
	
	public Arquivo(File arquivoDisseminar) throws IOException {	
		// para o disseminador gerar o arquivo para disseminar
		this.nome 		   		 = arquivoDisseminar.getName();
	    this.local 		   		 = arquivoDisseminar.getPath();
		this.tamanhoByte   		 = (int) arquivoDisseminar.length();
		this.arquivoFisico 		 = new RandomAccessFile(this.local, "rw");
		/*this.conteudo 	 = new byte[(int) arquivoDisseminar.length()];
			FileInputStream fis;
			fis = new FileInputStream(arquivoDisseminar);
	        fis.read(conteudo);
	        fis.close();*/
	    this.qtdPecas 	   		 = qtdPecas();
    	this.listaPecas    		 = geraListaPecasCompleta();
    	this.mapQtdPecasEnviadas = geraMapCompleto();
    	
	}
	
	public Arquivo(String nomeArquivoDownload, int tamanhoArquivoByte, String localDownload) {	
		// para o download gerar o arquivo a ser baixado
		this.nome 		   		 = nomeArquivoDownload;
    	this.local		   		 = localDownload;   
    	//this.conteudo 	 = new byte[(int) tamanhoArquivoByte];
    	this.tamanhoByte   		 = tamanhoArquivoByte; 
    	this.qtdPecas 	   		 = qtdPecas();
    	this.listaPecas    		 = new ArrayList<>();
    	this.mapQtdPecasEnviadas = new HashMap<>();
		try {
			this.arquivoFisico = new RandomAccessFile(this.local, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void addPecaLista(int numSeg) {
    	if (!listaPecas.contains(numSeg)) {		// se ainda não recebeu a peça 
    		listaPecas.add(numSeg);				// adiciona na lista
    		mapQtdPecasEnviadas.put(numSeg, 0);	// adiciona no map com envio 0
		}										// se nao descarta
    }
	
	public byte[] getPecaArquivo(int peca) throws IOException {
		int tam;
		
		if (peca == -1) {		// se -1 significa que não existe peça para ser enviada, entao enviar um datagrama vazio
			return new byte[1];
		}
		
		if (isUltimaPeca(peca)) {						// se for a ultima peça tem um tamanho menor
			tam = tamanhoByte - peca * Util.TAM_PACOTE_BYTE;
		} else {
			tam = Util.TAM_PACOTE_BYTE;
		}
		
		byte[] bytesPeca = new byte[tam];
	
		arquivoFisico.seek(peca * Util.TAM_PACOTE_BYTE);	// posiciona o ponteiro na posição do arquivo a ser lido
		arquivoFisico.read(bytesPeca);						// le o vetor de bytes na posição setada
		mapQtdPecasEnviadas.replace(peca, mapQtdPecasEnviadas.get(peca)+1);	// atualiza a quantidade de vezes que a peça foi enviada
		
		return bytesPeca;
	}
	
	public synchronized void addPecaArquivo(byte[] segmento, int peca) throws IOException {
		arquivoFisico.seek(peca * Util.TAM_PACOTE_BYTE);	// posiciona o ponteiro na posição do arquivo a ser escrito
		arquivoFisico.write(segmento);						// escreve o vetor na posição
	}
	
	public synchronized double geraPorcentagem() {
		double valor = ((listaPecas.size()*100.0)/qtdPecas);	// calcula a % de download do arquivo
		return valor;
	}
	
	/*public synchronized byte[] getPecaArquivo(int peca) {
		int tam, deslocamento;
		byte[] vetor;
		
		if (peca == -1) {
			return new byte[1];
		}
		
		if (isUltimaPeca(peca)) {						// se for a ultima peça tem um tamanho menor
			tam = conteudo.length - peca*Util.TAM_PACOTE_BYTE;
		} else {
			tam = Util.TAM_PACOTE_BYTE;
		}
		
		vetor =  new byte[tam];							// cria um vetor com todo o tamanho definido
		
		for (int i = 0; i < tam; i++) {
			deslocamento = peca * Util.TAM_PACOTE_BYTE;	// desloca para a posição onde inicia a peça selecionada
			vetor[i] = conteudo[deslocamento + i];
		}
		
		return vetor;
	}
	
	public synchronized void addPecaArquivo(byte[] segmento, int peca) {
		int tam;
		
		if (isUltimaPeca(peca)) {
			tam = conteudo.length - peca*Util.TAM_PACOTE_BYTE;
		} else {
			tam = Util.TAM_PACOTE_BYTE;
		}
		
		for (int i = 0; i < tam; i++) {
			conteudo[peca*Util.TAM_PACOTE_BYTE+i] = segmento[i];
		}
	}*/
	
	public int selecionaPecaRara(List<Integer> listaComPecas) {
		if (verificaListaCompleta(listaComPecas)) {		// se a lista recebida esta completa, nao envia nada
			return -1;
		}
		if (verificaListaContida(listaComPecas)) {		// se a lista recebida é igual a minha, nao envia nada
			return -1;
		}
		if (listaPecas.size() == 0) {					// se não tenho peças, nao envia nada
			return -1;
		}
		int sort = menosEnviado(listaComPecas);

		return sort;
	}
	
	private int menosEnviado(List<Integer> listaComPecas) {
		Iterator<Entry<Integer, Integer>> it = mapQtdPecasEnviadas.entrySet().iterator();
		int indMenor = 0;
		boolean prim = true;
		Map.Entry pair;
		
		while (it.hasNext()) {				// faz iteção em todo o map para buscar a menos enviada
			pair = (Map.Entry) it.next();
			if (prim) {
				if (!listaComPecas.contains((int) pair.getKey())) {		// so para pegar a primeira peça que nao foi enviada
					indMenor = (int) pair.getKey();
					prim = false;
				}
			} else {
				if ((int) pair.getValue() < mapQtdPecasEnviadas.get(indMenor) && !listaComPecas.contains((int) pair.getValue())) {
					indMenor = (int) pair.getKey();	// se a quantidade de vezes que foi enviada é menor que a atual atualiza
				}
				if ((int) pair.getValue() == mapQtdPecasEnviadas.get(indMenor) && !listaComPecas.contains((int) pair.getValue())) {
					int probTroca = (int) (Math.random()*10);
					if (probTroca < 2) {		// se valor de aceitação é valido e os valores de envio sao iguais atualiza			
						indMenor = (int) pair.getKey();
					}
				}
			}
		}
		
		return indMenor;
	}
	
	public boolean verificaListaCompleta(List<Integer> listaComPecas) {
		for (int i = 0; i < qtdPecas; i++) {	// verifica se todas as peças na lista foram recebidas
			if (!listaComPecas.contains(i)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean verificaListaContida(List<Integer> listaComPecas) {
		// utilizada para retornar um segmento vazio caso a lista do servidor esteja contida na lista recebida
		for (int i = 0; i < listaPecas.size(); i++) {	
			if (!listaComPecas.contains(listaPecas.get(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean verificaArquivoCompleto() {
		for (int i = 0; i < qtdPecas; i++) {	// verifica se todas as peças do arquivo ja foram recebidas
			if (!listaPecas.contains(i)) {
				return false;
			}
		}
		
		return true;
	}
	
	public String criaStringListaPecas() {
		// utilizada para criar uma string que será enviada em um segmento para um servidor 
		String lista = "";
		
		for (int i = 0; i < listaPecas.size(); i++) {
			lista += listaPecas.get(i) + ";";
		}		
		
		return lista;
	}
	
	private List<Integer> geraListaPecasCompleta() {
		// utilizado para o disseminador iniciar sua lista com todos as peças na lista
		List<Integer> lista  = new ArrayList<>();
		
		for (int i = 0; i < qtdPecas; i++) {
			lista.add(i);
		}
		
		return lista;
	}
	
	private Map<Integer, Integer> geraMapCompleto() {
		// utilizado para o disseminador iniciar sua lista com todos as peças com envio 0
		Map<Integer, Integer> map = new HashMap<>();
		
		for (int i = 0; i < qtdPecas; i++) {
			map.put(i, 0);						// o indice será a chave e o valor a qtd
		}
		
		return map;
	}
	
	private int qtdPecas() {
		double nPecas = tamanhoByte/(Util.TAM_PACOTE_BYTE);
		int result;
		
		if (Math.round(nPecas) < nPecas) {				// se existir restos na divisao o ultimo pacote conterá o resto
			result = (int) (Math.round(nPecas) + 1);
		} else {
			result = (int) (Math.round(nPecas));
		}

		return result;
	}
	
	private String geraHash(File f) throws NoSuchAlgorithmException, FileNotFoundException {
		// calcula o Hash do de todos os bytes do arquivo para adicionar no arquivo de metadados
		MessageDigest digest = MessageDigest.getInstance("MD5");
		InputStream is = new FileInputStream(f);				
		byte[] buffer = new byte[8192];
		int read = 0;
		String output = null;
		try {
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}		
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			output = bigInt.toString(16);
			//System.out.println("MD5: " + output);
		}
		catch(IOException e) {
			throw new RuntimeException("Não foi possivel processar o arquivo.", e);
		}
		finally {
			try {
				is.close();
			}
			catch(IOException e) {
				throw new RuntimeException("Não foi possivel fechar o arquivo", e);
			}
		}	
		return output;
	}
	
	public String geraChecksum() throws NoSuchAlgorithmException, FileNotFoundException {
		// calcula o Hash do de todos os bytes do arquivo para adicionar no arquivo de metadados
		return geraHash(new File(this.local));
	}
	
	private boolean isUltimaPeca(int peca) {
		// verifica se é a ultima peça, pois precisa de um tratamento especial
		if (peca == qtdPecas-1) {
			return true;
		} else {
			return false;
		}
	}

	public String getNome() {
		return nome;
	}

	/*public byte[] getConteudo() {
		return conteudo;
	}*/

	public int getTamanhoByte() {
		return tamanhoByte;
	}

	public int getQtdPecas() {
		return qtdPecas;
	}

	public String getLocal() {
		return local;
	}

	public List<Integer> getListaPecas() {
		return listaPecas;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	/*public void setConteudo(byte[] conteudo) {
		this.conteudo = conteudo;
	}*/

	public void setTamanhoByte(int tamanhoByte) {
		this.tamanhoByte = tamanhoByte;
	}

	public void setQtdPecas(int qtdPecas) {
		this.qtdPecas = qtdPecas;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public void setListaPecas(List<Integer> listaPecas) {
		this.listaPecas = listaPecas;
	}

	public void downloadFinalizado() throws IOException {
		arquivoFisico.close();
	}
}
