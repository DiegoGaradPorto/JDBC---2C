package lsi.ubu.excepciones;



import java.sql.SQLException;



import org.slf4j.Logger;

import org.slf4j.LoggerFactory;



/**

 * CompraBilleteTrenException: Implementa las excepciones contextualizadas de la

 * transaccion de CompraBilleteTren

 * 

 * @author <a href="mailto:jmaudes@ubu.es">Jes�s Maudes</a>

 * @author <a href="mailto:rmartico@ubu.es">Ra�l Marticorena</a>

 * @version 1.0

 * @since 1.0

 */

public class CompraBilleteTrenException extends SQLException {



	private static final long serialVersionUID = 1L;



	private static final Logger LOGGER = LoggerFactory.getLogger(CompraBilleteTrenException.class);



	public static final int NO_PLAZAS = 1;

	public static final int NO_EXISTE_VIAJE = 2;

	

	//Definición de excepciones personalizadas para el método "modificarBillete"

	public static final int NO_EXIXTE_BILLETE = 3;

	public static final int PLAZAS_NEGATIVAS = 4;

	public static final int PLAZAS_SUPERAN_LIMITE = 5;



	private int codigo; // = -1;

	private String mensaje;



	public CompraBilleteTrenException(int code) {

		

		//A completar por el alumno

		

		this.codigo = code;

		

		//Ceamos un switch-case para cada una de las excepciones

		switch(this.codigo) {

		case NO_PLAZAS:

			this.mensaje = "No hay plazas suficientes.";

			break;

		case NO_EXISTE_VIAJE:

			this.mensaje = "No existe ese viaje para esa fecha, hora, origen, destino.";

			break;

		case NO_EXIXTE_BILLETE:

			this.mensaje = "No existe el billete que se desea modificar.";

			break;

		case PLAZAS_NEGATIVAS:

			this.mensaje = "El número de plazas que se desea modificar es negativo.";

			break;

		case PLAZAS_SUPERAN_LIMITE: 

			this.mensaje = "El número de plazas que se desea modificar super el límite.";

			break;

		}



		LOGGER.debug(mensaje);



		// Traza_de_pila

		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {

			LOGGER.debug(ste.toString());

		}

	}



	@Override

	public String getMessage() { // Redefinicion del metodo de la clase Exception

		return mensaje;

	}



	@Override

	public int getErrorCode() { // Redefinicion del metodo de la clase SQLException

		return codigo;

	}

}

