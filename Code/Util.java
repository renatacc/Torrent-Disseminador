
public class Util {

	public static final int ENDERECO_PORTO_TRACKER = 1717;
	public static final int ENDERECO_PORTO_SERVIDOR = 1709;
	public static final int QTD_PECA_SOLICITACAO = 5;
	public static final int TAM_PACOTE_KB = 256;
	public static final int TAM_PACOTE_BYTE = 1024*TAM_PACOTE_KB;
	public static final int TEMPO_PENALIDADE = 10000;
	
	public static int pegaPosicao(String linha, String caracter) {
		return linha.indexOf(caracter);
	}
}
