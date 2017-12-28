import java.io.Serializable;

public class Segmento implements Serializable {
	// objeto que é utilizado para enviar os segmentos do servidor para o cliente
	private static final long serialVersionUID = 1L;
	private int numSeg;			// armazena qual o numero da peça
	private byte[] vetorPeca;	// armazena o vetor de segmento referente a peça
	private String checksum;	// checksum do segmento recebido
	
	public Segmento(int numSeg, byte[] vetorPeca, String checksum) {
		this.numSeg    = numSeg;
		this.vetorPeca = vetorPeca;
		this.checksum  = checksum;
	}

	public int getNumSeg() {
		return numSeg;
	}

	public byte[] getVetorPeca() {
		return vetorPeca;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setNumSeg(int numSeg) {
		this.numSeg = numSeg;
	}

	public void setVetorPeca(byte[] vetorPeca) {
		this.vetorPeca = vetorPeca;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
}
