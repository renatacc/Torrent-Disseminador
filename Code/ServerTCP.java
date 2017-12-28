import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ServerTCP extends Thread {
	
	private Arquivo arquivoDisseminado;
	private ListaSeed listaSeeds;
	
	public ServerTCP(ListaSeed listaSeeds, Arquivo arquivoDisseminado) {
		this.listaSeeds 		= listaSeeds;
		this.arquivoDisseminado = arquivoDisseminado;
		
		start();
	}
	
	@Override
	public void run() {
		Socket conexao;
		ServerSocket servidor;
		boolean flagTerminar = false;
		
		try {
			servidor = new ServerSocket(Util.ENDERECO_PORTO_SERVIDOR); 		// cria servidor TCP;
			System.out.println("=== Servidor iniciado!");
			
			while (!flagTerminar) {
				conexao = servidor.accept();								// solicitação SYN por algum cliente
				
				new SessaoServidor(conexao, arquivoDisseminado, listaSeeds);// passa para a Thread responder
			}
			System.out.println("=== Servidor finalizado!");
		} catch(Exception e) {	
			System.err.println("ERRO: " + e.toString());
		}
	}
}

class SessaoServidor extends Thread {
	
	private Socket conexao;
	Arquivo arquivoDisseminado;
	private ListaSeed listaSeeds;
	
	public SessaoServidor(Socket conexao, Arquivo arquivoDisseminado, ListaSeed listaSeeds) {
		this.arquivoDisseminado = arquivoDisseminado;	// recebe o arquivo para responder peças
		this.listaSeeds 		= listaSeeds;
		this.conexao 			= conexao;
		
		//System.out.println("Server Conexão recebida de: " + conexao.getInetAddress() + ":" + conexao.getPort());
		this.listaSeeds.addSeedLista(new Seed(conexao.getInetAddress().getHostAddress()));	// adiciona o cliente na lista
		
		start();
	}
	
	@Override
	public void run() {
		ObjectInputStream entrada;		// obtém o fluxo de entrada da conexao aberta
		ObjectOutputStream saida;		// obtém o fluxo de saida da conexao aberta
		SegmentoListaPecas objRecebido;	// objeto lista a ser recebido
		Segmento objResposta;			// objeto a ser respondido com o segmento
		List<Integer> listaPecas;
		int pecaRara;
		byte[] segmento;
		
		try {
			entrada = new ObjectInputStream(conexao.getInputStream());					// abre conexao para leitura
			saida  = new ObjectOutputStream(conexao.getOutputStream());					// abre conexao para escrita
			for (int i = 0; i < Util.QTD_PECA_SOLICITACAO; i++) {
				objRecebido = (SegmentoListaPecas) entrada.readObject();				// recebe objeto
			    listaPecas  = remontaListaPecas(objRecebido.getLista());				// convete objeto para lista de peças
			    
			    pecaRara    = arquivoDisseminado.selecionaPecaRara(listaPecas);			// sorteia peça rara
			    segmento 	= arquivoDisseminado.getPecaArquivo(pecaRara);				// pega segmento da peça
			    objResposta = new Segmento(pecaRara, segmento, geraChecksum(segmento));	// gera objeto para responder
			    System.out.println("Server Peça Enviada: " + pecaRara);
			    saida.flush();															// limpa o fluxo de saida
				saida.writeObject(objResposta);											// responde com a peça
			}
			conexao.close(); 	// solicitação de FIN
			
		} catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
			System.out.println("Erro Sessão Servidor");
			e.printStackTrace();
		}
	}
	
	private String geraChecksum(byte[] segmento) throws NoSuchAlgorithmException {
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(segmento, 0, segmento.length);
		
		return new BigInteger(1, m.digest()).toString(16);
	}
	
	private List<Integer> remontaListaPecas(String dados) {
		List<Integer> listaPecas = new ArrayList<>();
		int i;
		
		while (dados.length() > 0) {
			// pega numero peça
			i = Util.pegaPosicao(dados, ";");
			listaPecas.add(Integer.parseInt(dados.substring(0,i)));
			dados = dados.substring(i+1, dados.length());
		}
		
		return listaPecas;
	}
}