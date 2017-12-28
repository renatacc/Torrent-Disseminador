
import java.net.*;
import java.io.*;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class Tracker extends Thread {
	
	private ListaSeed listaSeeds;		// lista com todos os ips e portos no enxame
	private DatagramSocket soquete;
	
	public Tracker(String origem, ListaSeed listaSeeds) {
		this.listaSeeds = listaSeeds;
		this.listaSeeds.addSeedLista(new Seed(origem));
		
		start();
	}
	
	@Override
	public void run() {
		boolean flagTerminar = false;
		
		try {
			soquete = new DatagramSocket(Util.ENDERECO_PORTO_TRACKER);
			System.out.println("=== Tracker Iniciado!");
			
			String endIPTracker   = InetAddress.getLocalHost().getHostAddress();
			int portoTracker 	  = soquete.getLocalPort();

			System.out.println("Endere�o do Tracker: " + endIPTracker);
			System.out.println("Porto do Tracker: " + portoTracker);
			
			// buffer de entrada
			byte[] bufferEntrada = new byte[256];
			DatagramPacket requisicao = new DatagramPacket(bufferEntrada, bufferEntrada.length);

			// buffer de saida: o buffer ter� o pr�prio tamanho da String
			byte[] bufferSaida;
			DatagramPacket resposta;
			
			do {
				// limpa o buffer de entrada antes do recebimento de uma nova requisi��o (evita o "lixo" da requisi��o anterior)
				for(int i = 0; i < bufferEntrada.length; i++) {
					bufferEntrada[i] = 0;
				}
				
				// aguarda o recebimento de um datagrama (requisi��o do cliente)
				soquete.receive(requisicao);
				// obt�m os dados do remetente a partir do CABE�ALHO do datagrama
				InetAddress endCliente = requisicao.getAddress();
				String endIPCliente    = endCliente.getHostAddress();
				int portoCliente 	   = requisicao.getPort();
				
				//System.out.println("\n <<< Requisi��o recebida");
				//System.out.println("\t Cliente: " + endIPCliente + " Porto: " + portoCliente);

				// obt�m os dados da requisicao a partir do CONTE�DO do datagrama
				String dados = new String(requisicao.getData()).trim();
				//System.out.println("\t Conte�do: "+ dados);
				
				if (dados.equals("SEEDS")){	// se a solicita��o for seed, significa que quer participar do enxame
					Seed newSeed = new Seed(endIPCliente);
					/*if (!listaSeeds.contains(newSeed)) {
						listaSeeds.addSeedLista(newSeed);
					}*/
					// obt�m a a lista de seeds e a envia para o cliente
					String lista = listaSeeds.listaSeedsToString();
					// bytes  enviados: o buffer conter� os caracteres da String
					bufferSaida = lista.getBytes();
					
					// cria o pacote (datagrama) que conter� a resposta do servidor
					resposta = new DatagramPacket(bufferSaida, bufferSaida.length, endCliente, portoCliente);
					// envia a resposta ao cliente
					soquete.send(resposta);
					
					if (!listaSeeds.contains(newSeed)) {
						listaSeeds.addSeedLista(newSeed);
					}
					
					System.out.println(" >>> Tracker Resposta enviada");
					System.out.println("\t Conte�do: " + lista);
					System.out.println("\t Para:     " + endIPCliente);
				}
				
			} while(!flagTerminar);
			System.out.println("=== Tracker finalizado!" );
		} catch (IOException e) {
			System.err.println("ERRO: " + e.toString());
		}
	}

	
}