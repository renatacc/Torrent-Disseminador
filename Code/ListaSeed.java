import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ListaSeed {
	
	List<Seed> listaSeeds;		// lista com o endereço de todos os seeds do enxame
	
	public ListaSeed() {
		listaSeeds = new ArrayList<>();
	}
	
	public synchronized void addSeedLista(Seed newSeed) {
		// para adicionar alguem a lista é verificado se ele ainda não está na lista
		if (!listaSeeds.contains(newSeed)) {
			listaSeeds.add(newSeed);
		}
	}
	
	public synchronized boolean contains(Seed newSeed) {
		// verifica se a lista ainda não contem o seed
		if (listaSeeds.contains(newSeed)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Seed sorteiaEndereco() throws UnknownHostException {
		int sort = (int) (Math.random()*listaSeeds.size());
		// sorteia um seed para conexão, porém este nao pode ser o mesmo que o cliente, e também nao pode estar em penalização
		while (listaSeeds.get(sort).getEndereco().getHostAddress().equals(InetAddress.getLocalHost().getHostAddress()) &&
					listaSeeds.get(sort).getPenalidade() > System.currentTimeMillis()) {
			sort = (int) (Math.random()*listaSeeds.size());
		}
		 
		return listaSeeds.get(sort);
	}
	
	public String listaSeedsToString() {
		// monta uma string com todos os clientes que estão conectados ao enxame
		String dados = "";
		
		for (int i = 0; i < listaSeeds.size(); i++) {
			dados+=listaSeeds.get(i)+"\n";
		}
		
		return dados;
	}
	
	public static ListaSeed geraListaSeeds(String dados) {
		ListaSeed lista = new ListaSeed();
		String endIP;
		int i;
		Seed newSeed;
		
		while (dados.length() > 5) {
			// pega o IP do seed
			i 	  = Util.pegaPosicao(dados, ";");
			endIP = dados.substring(0,i);
			dados = dados.substring(i+1, dados.length());
			// apaga resto da linha
			i 	  = Util.pegaPosicao(dados, "\n");
			dados = dados.substring(i+1, dados.length());
			
			newSeed = new Seed(endIP);			// cria novo Seed
			if(!lista.contains(newSeed)) {
				lista.addSeedLista(newSeed);	// adiciona seus dados a lista
			}			
		}
		
		return lista;
	}
}
