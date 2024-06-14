package lsi.ubu;



import org.slf4j.Logger;

import org.slf4j.LoggerFactory;



import lsi.ubu.tests.Tests;

import lsi.ubu.util.ExecuteScript;



/**

 * ComprarBillete: Implementa el la compra de un billete de Tren controlando si

 * quedan plazas libres y si existe el viaje segun PDF de la carpeta enunciado

 *

 * AnularBillete: Anula la compra de un billete de Tren controlando si existe el

 * viaje y el ticket segun PDF de la carpeta enunciado

 * 

 * @author <a href="mailto:jmaudes@ubu.es">Jes�s Maudes</a>

 * @author <a href="mailto:rmartico@ubu.es">Ra�l Marticorena</a>

 * @author <a href="mailto:srarribas@ubu.es">Sandra Rodríguez</a>

 * @version 1.0

 * @since 1.0

 */

public class CompraBilleteTren {



	private static final Logger LOGGER = LoggerFactory.getLogger(CompraBilleteTren.class);



	public static void main(String[] args) {



		LOGGER.info("Comienzo de los tests");



		// Crear las tablas y filas en base de datos para la prueba

		ExecuteScript.run("sql/CompraBilleteTren.sql");



		// Ejecutar tests comprar 

		Tests tests = new Tests();

		LOGGER.info("");

		LOGGER.info(""); //Varios saltos de linea para diferenciar entre los mensajes de los procesos de compra y anulacion

		LOGGER.info("");

		LOGGER.info("Comienzan los test de COMPRAR BILLETES");

		tests.ejecutarTestsCompraBilletes();

		

		//



		// Crear las tablas y filas en base de datos para la prueba

		ExecuteScript.run("sql/CompraBilleteTren.sql");



		// Ejecutar tests anular 

		LOGGER.info("");

		LOGGER.info(""); //Varios saltos de linea para diferenciar entre los mensajes de los procesos de compra y anulacion

		LOGGER.info("");

		LOGGER.info("Comienzan los test de ANULAR BILLETES");

		tests.ejecutarTestsAnularBilletes();

		

		//

		

		// Crear las tablas y filas en base de datos para la prueba

		ExecuteScript.run("sql/CompraBilleteTren.sql");

		

		//Ejecutar test de modificacion

		LOGGER.info("");

		LOGGER.info(""); //Varios saltos de linea para diferenciar entre los mensajes de los procesos de compra y anulacion

		LOGGER.info("");

		LOGGER.info("Comienzan los test de MODIFICAR BILLETES");

		tests.ejecutarTestsModificarBilletes();

		

		LOGGER.info("Fin de los tests");

		

	}

}

