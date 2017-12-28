

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Seed {
	
	private InetAddress endereco;	// endereço do seed
	private long penalidade;		// tempo de penalidade
	// quando um seed é penalizado ele recebe em qual tempo de execução ele poderá ser utilizado para solicitar novas peças

	public Seed(String endereco) {
		try {
			this.endereco = InetAddress.getByName(endereco);
			this.penalidade = System.currentTimeMillis();	// recebe o tempode execução atual pois ja pode receber conexões
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
