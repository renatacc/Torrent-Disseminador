import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FrameHome extends JFrame {

	private static final long serialVersionUID = 1L;
	private JButton btnDisseminar;
	private JButton btnBaixar;
	private JPanel panel;
	private ListaSeed listaSeeds = new ListaSeed();
	
	public FrameHome() {
		super("TriTorrent");
		panel = new JPanel();
		btnDisseminar = new JButton("Disseminar Arquivo");
		btnDisseminar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Selecione o arquivo para Disseminar");
				Arquivo arqDisseminar = selecionaArquivoDisseminacao();
				if (arqDisseminar != null) {
					System.out.println("Selecione onde salvar o arquivo de metaDados");
					geraArquivoMetaDados(arqDisseminar);
				    iniciaDisseminacao(arqDisseminar);
				} else {
					System.out.println("Erro para Disseminar o arquivo selecionado");
				}
			}
		});
		
		btnBaixar = new JButton("Baixar Torrent");
		btnBaixar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				iniciaDownload();
			}
		});
		
		panel.add(btnDisseminar);
		panel.add(btnBaixar);
		add(panel);
		
		setSize(300,100);
		setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
	}

	private void iniciaDownload() {
		String local;
		try {
			System.out.println("Selecione arquivo de metaDados");
			ArquivoMetaDados arquivoMeta = selecionaArquivoMetaDados();
			solicitaListaSeedsUDP(arquivoMeta.getEnderecoTracker());
			System.out.println("Selecione onde salvar o arquivo de Download");
			local = selecionaLocalDownload();
			local = local + "\\"+ arquivoMeta.getNome();
			
			System.out.println("Lista Recebida: " + listaSeeds.listaSeedsToString());
			
			Arquivo arq = new Arquivo(arquivoMeta.getNome(), arquivoMeta.getTamanhoByte(), local);
			
			new ClienteTCP(listaSeeds, arq);	// inicia o cliente
			new ServerTCP(listaSeeds, arq);		// inicia o server
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void solicitaListaSeedsUDP(String endTracker) {
		DatagramSocket soquete;
		byte[] bufferSaida;
		byte[] bufferEntrada;
		DatagramPacket resposta;
		
		try {
			soquete = new DatagramSocket();
			String dados = "SEEDS"; 						// comando a ser enviado pelo cliente para o Tracker
			bufferSaida = dados.getBytes();					// o buffer terá o próprio tamanho da String
			// cria o pacote (datagrama) com a requisição a ser enviada, endereçada ao servidor
			DatagramPacket requisicao = new DatagramPacket(bufferSaida, bufferSaida.length, 
															InetAddress.getByName(endTracker), Util.ENDERECO_PORTO_TRACKER);
			
			soquete.send(requisicao);						// envia a requisição ao tracker
			
			bufferEntrada = new byte[512];					// o buffer cabera 512 bytes de resposta
	
			// cria o pacote (datagrama) que conterá a resposta do servidor
			resposta = new DatagramPacket(bufferEntrada, bufferEntrada.length); 
			
			soquete.receive(resposta);						// aguarda resposta do servidor
			// obtém os dados da resposta a partir do conteudo do datagrama
			dados = new String(resposta.getData()).trim(); 	// trim() remove espaços desnecessários antes e depois (buffer >= dados)
			
			listaSeeds = ListaSeed.geraListaSeeds(dados);	// gera a lista de seeds recebida do tracker
			
		} catch (Exception e) {
			System.err.println("ERRO: " + e.toString());
		}
	}

	private String selecionaLocalDownload() {
		JFileChooser file = new JFileChooser();
		int i;
		
		file = new JFileChooser();	
		
		file.setDialogType(JFileChooser.DIRECTORIES_ONLY);
	    i = file.showSaveDialog(null);						// abre janela para seleção
	    if (i==1){
	    	JOptionPane.showMessageDialog(null, "Erro ao selecionar onde salvar o arquivo");
	    } else {
	    	return file.getCurrentDirectory().getPath();
	    }
	    
	    return null;
	}

	private ArquivoMetaDados selecionaArquivoMetaDados() throws IOException {
		ArquivoMetaDados arquivoMeta = null;
		JFileChooser file = new JFileChooser();				// arquivo meta dados para iniciar download
	    String linha;
		int i;
		
		file = new JFileChooser();		
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".triTor", "triTor");
		file.setFileFilter(filter);
		
		file.setDialogType(JFileChooser.OPEN_DIALOG);
	    i = file.showOpenDialog(null);						// abre janela para seleção
	    if (i==1){
	    	JOptionPane.showMessageDialog(null, "Erro ao abrir o arquivo de metadados");
	    } else {
	    	arquivoMeta = new ArquivoMetaDados();			// cria um objeto arquivo 
	    	File arquivo = file.getSelectedFile();			//pega o nome do arquivo
			Reader reader = new FileReader(arquivo);		//abre o arquivo para leitura
			//************************************************************************
			BufferedReader lerArq = new BufferedReader(reader);
			linha = lerArq.readLine();	// le nome
			arquivoMeta.setNome(linha);
			linha = lerArq.readLine();	// le tamanho bytes
			arquivoMeta.setTamanhoByte(Integer.parseInt(linha));
			linha = lerArq.readLine();	// le endereco Tracker
			arquivoMeta.setEnderecoTracker(linha);
			linha = lerArq.readLine();	// le checksum
			arquivoMeta.setChecksum(linha);
			//************************************************************************
			reader.close();
	    }
		
		
		return arquivoMeta;
	}

	private Arquivo selecionaArquivoDisseminacao() {
		Arquivo arquivo = null;
		
		try {
			JFileChooser file = new JFileChooser();				// arquivo que sera aberto para disseminação
			int i;
			
			file.setDialogType(JFileChooser.OPEN_DIALOG);
		    i = file.showOpenDialog(null);						// abre janela para seleção
		    if (i==1){
		    	JOptionPane.showMessageDialog(null, "Erro ao Abrir o arquivo para Disseminar");
				return null;
		    } else {
		    	arquivo = new Arquivo(file.getSelectedFile());	// cria um objeto arquivo 
		    }
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return arquivo;
	} 
	
	private void geraArquivoMetaDados(Arquivo arqDisseminar) {
		try {
			JFileChooser file = new JFileChooser();
			int i;
			
			file = new JFileChooser();		
			FileNameExtensionFilter filter = new FileNameExtensionFilter(".triTor", "triTor");
			file.setFileFilter(filter);
			
			file.setDialogType(JFileChooser.SAVE_DIALOG);						// abre janela para salvar arquivo meta dados
		    i = file.showSaveDialog(null);
		    if (i==1){
		    	JOptionPane.showMessageDialog(null, "Erro ao Salvar o arquivo de Meta Dados");
		    } else {
		    	File arquivoMetaDados = file.getSelectedFile();					// pega o arquivo
		    	
		    	Writer writer;
				writer = new FileWriter(arquivoMetaDados + ".triTor");			// abre um arquivo para escrita
				//********************************************************************************
		        writer.write(arqDisseminar.getNome()+"\n");						// escreve o nome do arquivo
		        writer.write(arqDisseminar.getTamanhoByte()+"\n");				// escreve o tamanho do arquivo
		        writer.write(InetAddress.getLocalHost().getHostAddress()+"\n");	// escreve o ip do tracker
		        writer.write(arqDisseminar.geraChecksum()+"\n");				// escreve o checksum do arquivo
		        //********************************************************************************
				writer.close();													//fecha o arquivo
		    }
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private void iniciaDisseminacao(Arquivo arqDisseminar) {
		try {
			new Tracker(InetAddress.getLocalHost().getHostAddress(), listaSeeds);
			Thread.sleep(100);
			new ServerTCP(listaSeeds, arqDisseminar);
		} catch (UnknownHostException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
