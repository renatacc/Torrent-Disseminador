import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClienteTCP extends Thread {

	private Arquivo arquivoDownload;
	private ListaSeed listaSeeds;
	
	public ClienteTCP(ListaSeed listaSeeds, Arquivo arquivoDownload) {
		this.listaSeeds 	 = listaSeeds;		// lista de seeds para solicitar peças
		this.arquivoDownload = arquivoDownload; // Objeto que contem os dados do arquivo a ser baixado
		
		start();
	}
	
	@Override
	public void run() {
		long inicio, fim, dif;
		
		System.out.println("=== Cliente iniciado!");
		
		inicio = System.currentTimeMillis();
		while (!arquivoDownload.verificaArquivoCompleto()) {	// enquanto o arquivo nao estiver completo continua solicitando novas peças
			solicitaPecas();
		}
		fim = System.currentTimeMillis();
		dif = (fim - inicio);
		System.out.printf("Tempo de Download: %02d minutos  e %02d segundo\n", dif/60000, dif%60000/1000);
		
		System.out.println("=== Download Finalizado");
	}
	
	private void solicitaPecas() {
		try { // instancia 10 threads para solicitar novas peças
			new SessaoCliente(listaSeeds.sorteiaEndereco(), arquivoDownload);
			new SessaoCliente(listaSeeds.sorteiaEndereco(), arquivoDownload);
			new SessaoCliente(listaSeeds.sorteiaEndereco(), arquivoDownload);
			new SessaoCliente(listaSeeds.sorteiaEndereco(), arquivoDownload);
			new SessaoCliente(listaSeeds.sorteiaEndereco(), arquivoDownload);
			new SessaoCliente(listaSeeds.sorteiaEndereco(), arquivoDownload);
			new SessaoCliente(listaSeeds.sorteiaEndereco(), arquivoDownload);
			new SessaoCliente(listaSeeds.sorteiaEndereco(), arquivoDownload);
			new SessaoCliente(listaSeeds.sorteiaEndereco(), arquivoDownload);
			new SessaoCliente(listaSeeds.sorteiaEndereco(), arquivoDownload);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}

class SessaoCliente extends Thread {
	
	public SessaoCliente(Seed seedEnd, Arquivo arquivoDownload) {
		Socket conexao;
		SegmentoListaPecas objEnviar;	// objeto lista a ser enviado
		Segmento objRecebido;			// objeto a ser respondido com o segmento
		ObjectInputStream entrada;		// obtém o fluxo de entrada da conexao aberta
		ObjectOutputStream saida;		// obtém o fluxo de saida da conexao aberta
		
		try {
			conexao = new Socket(seedEnd.getEndereco().getHostAddress(), Util.ENDERECO_PORTO_SERVIDOR);
			//System.out.println("Cliente Conexão feita com: " + conexao.getInetAddress() + ":" + conexao.getPort());
			
			saida = new ObjectOutputStream(conexao.getOutputStream());	// abre conexao para escrita
			entrada = new ObjectInputStream(conexao.getInputStream());	// abre conexao para ler
			for (int i = 0; i < Util.QTD_PECA_SOLICITACAO; i++) {
				long inicio = System.currentTimeMillis();
				saida.flush();
				objEnviar = new SegmentoListaPecas(arquivoDownload.criaStringListaPecas());					// cria objeto com a lista de peças
				saida.writeObject(objEnviar);																// envia a lista para o servidor
				
				objRecebido = (Segmento) entrada.readObject();												// recebe um segmento do servidor
				long fim = System.currentTimeMillis();
				long dif = (fim - inicio);
				double seg = (dif/1000.0);
				double vel = (((double) Util.TAM_PACOTE_BYTE) / seg);										// calcula a velocidade de download
				
				if (objRecebido.getNumSeg() != -1) {														// se não é um segmento vazio
					if (!arquivoDownload.getListaPecas().contains(objRecebido.getNumSeg())) {				// se ainda não contem esta peça
						if (verificaChecksum(objRecebido.getChecksum(), objRecebido.getVetorPeca())) {		// verifica o checksum do segmento
							arquivoDownload.addPecaLista(objRecebido.getNumSeg());							// adiciona a peça na lista de pecas
							arquivoDownload.addPecaArquivo(objRecebido.getVetorPeca(), objRecebido.getNumSeg());// adiciona a peça no arquivo
							System.out.printf("Cliente Peça Recebida: %d Baixado: %.2f %% Vel: %.2f Kb/s \n", 
												objRecebido.getNumSeg(), arquivoDownload.geraPorcentagem(), 
												vel/1024.0);
						}
					}
				} else {
					seedEnd.setPenalidade(System.currentTimeMillis() + Util.TEMPO_PENALIDADE);		// se o seed nao tem peças para mim penalizo ele
					//break;
				}
			}
			conexao.close();
		} catch (IOException | NoSuchAlgorithmException | ClassNotFoundException e) {
			System.out.println("Erro Sessão Cliente");
			//seedEnd.setPenalidade(System.currentTimeMillis() + Util.TEMPO_PENALIDADE); 				// se ocorreu um erro na conexao penalizo ele
			e.printStackTrace();
		}
	}
	
	private boolean verificaChecksum(String checksum, byte[] segmento) throws NoSuchAlgorithmException {
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(segmento, 0, segmento.length);
		
		String newChecksum = new BigInteger(1, m.digest()).toString(16);
		
		if (checksum.equals(newChecksum)) {	// compara o checksum recebido com o gerado do segmento
			
			return true;
		}
		
		return false;
	}
}