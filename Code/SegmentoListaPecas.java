import java.io.Serializable;

public class SegmentoListaPecas implements Serializable {
	// objeto utilizado para enviar a lista de peças do cliente para o servidor
	private static final long serialVersionUID = 1L;
	private String lista;	// armazena a lista de peças
	
	public SegmentoListaPecas(String lista) {
		this.lista = lista;
	}

	public String getLista() {
		return lista;
	}
}
