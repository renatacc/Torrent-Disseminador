
public class ArquivoMetaDados {
	private String nome;			// nome do arquivo a disseminar
    private int tamanhoByte;		// tamanho do arquivo a disseminar
    private String enderecoTracker;	// endereço do tracker onde encontrar a lista de seeds
	private String checksum;		// checksum do arquivo para comparação final
	
	public ArquivoMetaDados() {
		
	}

	public String getNome() {
		return nome;
	}

	public int getTamanhoByte() {
		return tamanhoByte;
	}

	public String getEnderecoTracker() {
		return enderecoTracker;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setTamanhoByte(int tamanhoByte) {
		this.tamanhoByte = tamanhoByte;
	}

	public void setEnderecoTracker(String enderecoTracker) {
		this.enderecoTracker = enderecoTracker;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
}
