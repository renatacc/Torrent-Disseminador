

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Seed {
	
	private InetAddress endereco;	// endere�o do seed
	private long penalidade;		// tempo de penalidade
	// quando um seed � penalizado ele recebe em qual tempo de execu��o ele poder� ser utilizado para solicitar novas pe�as

	public Seed(String endereco) {
		try {
			this.endereco = InetAddress.getByName(endereco);
			this.penalidade = System.currentTimeMillis();	// recebe o tempode execu��o atual pois ja pode receber conex�es
		} catch (UnknownHostException e) {
			System.err.println("ERRO: " + e.toString());
		}
	}

	public InetAddress getEndereco() {
		return endereco;
	}

	public void setEndereco(InetAddress endereco) {
		this.endereco = endereco;
	}

	public long getPenalidade() {
		return penalidade;
	}

	public void setPenalidade(long penalidade) {
		this.penalidade = penalidade;
	}

	@Override
	public String toString() {
		return endereco.getHostAddress()+";";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endereco == null) ? 0 : endereco.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Seed other = (Seed) obj;
		if (endereco == null) {
			if (other.endereco != null)
				return false;
		} else if (!endereco.equals(other.endereco))
			return false;
		return true;
	}	
}
