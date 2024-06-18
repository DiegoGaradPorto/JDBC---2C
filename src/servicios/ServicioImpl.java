package lsi.ubu.servicios;



import java.sql.Connection;



import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Time;

import java.util.Date;



import org.slf4j.Logger;

import org.slf4j.LoggerFactory;



import lsi.ubu.excepciones.CompraBilleteTrenException;

import lsi.ubu.util.PoolDeConexiones;



public class ServicioImpl implements Servicio {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServicioImpl.class);



	//Metodo anular billete

	@Override

	public void anularBillete(Time hora, java.util.Date fecha, String origen, String destino, int nroPlazas, int ticket)

			throws SQLException {

		PoolDeConexiones pool = PoolDeConexiones.getInstance();



		/* Conversiones de fechas y horas */

		java.sql.Date fechaSqlDate = new java.sql.Date(fecha.getTime());

		java.sql.Timestamp horaTimestamp = new java.sql.Timestamp(hora.getTime());



		Connection con = null;

		PreparedStatement st = null;

		ResultSet rs = null;



		// A completar por el alumno

		try {

			con = pool.getConnection();

			

			//Desactivamos el autocommit

			con.setAutoCommit(false);

		

			// 1º Probamos que exista el ticket

			st = con.prepareStatement("SELECT idTicket, idViaje, cantidad FROM tickets WHERE idTicket = ?");

			st.setInt(1, ticket);

			rs = st.executeQuery();

	

			// si no encuentra un ticket lanza la excepcion

			if (!rs.next()) 

				throw new CompraBilleteTrenException(CompraBilleteTrenException.NO_EXISTE_VIAJE);

			

			//Obtenemos el valor de las plazas que se van a liberar / anular

			int idViaje = rs.getInt("idViaje");

			int plazasLiberadas = rs.getInt("cantidad");

			

			

			// 2º Comprobamos que el viaje no se efectuo

			st = con.prepareStatement("SELECT realizado FROM viajes WHERE idViaje = ?");

			st.setInt(1, idViaje);

			rs = st.executeQuery();



			//Si el viaje ya se ha realizado se considera que no existe ese billetes de viajes pendientes que poder anular

			if (!rs.next() || rs.getInt("realizado") == 1) 

				throw new CompraBilleteTrenException(CompraBilleteTrenException.NO_EXISTE_VIAJE);

			



			// 3º ACtualizamos el numero de plazas que hay disponibles en el viaje 

			// Sumamos las plazasLibres + plazasLiberadas 

			st = con.prepareStatement("UPDATE viajes SET nPlazasLibres = nPlazasLibres + ? WHERE idViaje = ?");

			st.setInt(1, plazasLiberadas);

			st.setInt(2, idViaje);

			st.executeUpdate();



			// 4º Anulamos la reserva 

			st = con.prepareStatement("DELETE FROM tickets WHERE idTicket = ?");

			st.setInt(1, ticket);

			st.executeUpdate();



			// comiteamos la transaccion

			con.commit();



		//Tratamos la excepcion CompraBilleteTrenException

		}catch (CompraBilleteTrenException e) { 

			LOGGER.error("Error al anular el billete: " + e.getMessage());

			if (con != null) {

				con.rollback(); // Realizamos el rollback de la transacción

			}

			throw e;

			

		//Tratamos el resto de excepciones SQLException

		}catch (SQLException e) {

			LOGGER.error("Error al anular el billete: " + e.getMessage());

			if (con != null) {

				con.rollback(); // Realizamos el rollback de la transacción

			}

			throw e;

			

		//Liberamos los recursos utilizados	

		}finally {

		

			if (rs != null) {

				rs.close();

			}

			if (st != null) {

				st.close();

			}

			if (con != null) {

				con.close();

			}

		}

	}



	//Metodo comprar billete

	@Override

	public void comprarBillete(Time hora, Date fecha, String origen, String destino, int nroPlazas)

			throws SQLException {

		PoolDeConexiones pool = PoolDeConexiones.getInstance();



		/* Conversiones de fechas y horas */

		java.sql.Date fechaSqlDate = new java.sql.Date(fecha.getTime());

		java.sql.Timestamp horaTimestamp = new java.sql.Timestamp(hora.getTime());



		Connection con = null;

		PreparedStatement st = null;

		ResultSet rs = null;



		// A completar por el alumno

		

		//Comenzamos una transaccion

		try {



			con = pool.getConnection();

			

			//Desactivamos el Autocommit

			con.setAutoCommit(false);

			

			// Verificar si el viaje existe

			st = con.prepareStatement(

					"SELECT v.idViaje " +

							"FROM viajes v " +

							"INNER JOIN recorridos r ON v.idRecorrido = r.idRecorrido " +

							"WHERE r.estacionOrigen = ? AND r.estacionDestino = ? AND v.fecha = ?");



			st.setString(1, origen);

			st.setString(2, destino);

			st.setDate(3, fechaSqlDate);

			rs = st.executeQuery();



			if (!rs.next()) {

				// No se encontró el viaje

				throw new CompraBilleteTrenException(CompraBilleteTrenException.NO_EXISTE_VIAJE);

			}



			//Guardamos el valor del idViaje en una variable para poder trabajar con ella posteriormente

			int idViaje = rs.getInt("idViaje");



			// Verificar si hay plazas disponibles

			st = con.prepareStatement(

					"SELECT (m.nPlazas - COALESCE(SUM(t.cantidad), 0)) AS plazas_disponibles " +

							"FROM modelos m " +

							"INNER JOIN trenes tr ON m.idModelo = tr.modelo " +

							"LEFT JOIN viajes v ON tr.idTren = v.idTren " +

							"LEFT JOIN tickets t ON v.idViaje = t.idViaje " +

							"WHERE v.idViaje = ? " +

							"GROUP BY m.nPlazas");



			st.setInt(1, idViaje);

			rs = st.executeQuery();



			//Si el número de plazas disponibles es menor al número de plazas que se quieren comprar salta la excepción

			if (!rs.next() || rs.getInt("plazas_disponibles") < nroPlazas) {

				// No hay plazas suficientes

				throw new CompraBilleteTrenException(CompraBilleteTrenException.NO_PLAZAS);



			}



			// Actualizamos las plazas disponibles para ese viaje

			st = con.prepareStatement("UPDATE viajes SET nPlazasLibres = nPlazasLibres - ? WHERE idViaje= ?");

			st.setInt(1,nroPlazas);

			st.setInt(2,idViaje);

			st.executeUpdate();

			

			// Obtenemos el precio para dicho viaje

			st = con.prepareStatement(

					"SELECT r.precio " +

					"FROM recorridos r " +

					"INNER JOIN viajes v ON r.idRecorrido = v.idRecorrido " +

					"WHERE v.idViaje = ?");

			st.setInt(1, idViaje);

			rs = st.executeQuery();



			//Guardamos el valor del precio del recorrido en una variable

			int precioRecorrido = 0;

			if (rs.next()) {

				precioRecorrido = rs.getInt("precio");

			}

			

			// Calculamos el precio total del viaje

			precioRecorrido = rs.getInt("precio"); // Obtenemos el precio del recorrido



			// Calculamos el precio total

			int precioTotal = nroPlazas * precioRecorrido; 

			int valido = 0;

			

			

			// Insertamos la fila en la tabla de tickets

			st = con.prepareStatement(

					"INSERT INTO tickets (idTicket, idViaje, fechaCompra, cantidad, precio) " +

							"VALUES (seq_tickets.nextval, ?, ?, ?, ?)");



			st.setInt(1, idViaje);

			st.setDate(2, fechaSqlDate);

			st.setInt(3, nroPlazas);

			st.setInt(4, precioTotal);

			st.executeUpdate();

		

			//Comiteamos la transacción

			con.commit();



		//Tratamos las excepciones del tipo CompraBilleteTrenException	

		}catch (CompraBilleteTrenException e) { 

			LOGGER.error("Error al comprar el billete: " + e.getMessage());

			if (con != null) {

				con.rollback(); // Realizamos el rollback de la transacción

			}

			throw e;

			

		//Tratamos el resto de excepciones

		}catch (SQLException e) {

			LOGGER.error("Error al realizar la compra de billetes: " + e.getMessage());

			if (con != null) {

				con.rollback(); // Realizamos el rollback de la transacción

			}

			throw e;



		//Cerramos los recursos utilizados

		} finally {

			if (rs != null) {

				rs.close();

			}

			if (st != null) {

				st.close();

			}

			if (con != null) {

				con.close();

			}

		}

	}

	

	//Metodo modificar billete

	@Override

	public void modificarBillete(int billeteId, int nuevoNroPlazas) 

			throws SQLException {

		

		PoolDeConexiones pool = PoolDeConexiones.getInstance();



		Connection con = null;

		PreparedStatement st = null;

		ResultSet rs = null;

		

		//Comenzamos una transaccion

		try {

			

			con = pool.getConnection();

			

			//Desactivamos el Autocommit

			con.setAutoCommit(false);

			

			

			//Verificamos si existe el billete que se desea modificar

			st = con.prepareStatement(

					"SELECT t.idTicket, t.cantidad, t.precio " 

					+ "FROM tickets t "

					+ "WHERE t.idTicket = ?");

			st.setInt(1, billeteId);

			rs = st.executeQuery();

			

			if (!rs.next()) {

				// No se encontró el billete

				throw new CompraBilleteTrenException(CompraBilleteTrenException.NO_EXIXTE_BILLETE);

			}

			

			//Verificamos que el numero de plazas que se desea modificar es positivo

			if (nuevoNroPlazas < 0) {

				throw new CompraBilleteTrenException(CompraBilleteTrenException.PLAZAS_NEGATIVAS);

			}

			

			//Verificamos que el número de plazas que se desea modificar no exceda el número de plazas disponibles en ese viaje

			//Primero obtenemos el número de plazas disponibles para ese viaje

			st = con.prepareStatement(

					"SELECT v.nPlazasLibres "

					+ "FROM viajes v "

					+ "JOIN tickets t ON v.idViaje = t.idViaje "

					+ "WHERE idTicket = ? ");

			st.setInt(1,billeteId);

			rs = st.executeQuery();

			

			if(!rs.next()) {

				throw new SQLException("No se pudo obtener el número de plazas del viaje correspondiente a ese billete");

			}

			

			//Guardamos el valor del numero de plazas disponibles en una variable para trabajar con ella

			int plazasDisponibles = rs.getInt("nPlazasLibres");

			

			//Hacemos la verificacion de que la modificación no excede el numero de plazas libres

			if(nuevoNroPlazas > plazasDisponibles) {

				throw new CompraBilleteTrenException(CompraBilleteTrenException.PLAZAS_SUPERAN_LIMITE);

			}

			

			//Obtenemos el valor del precio para una plaza de dicho viaje

			st = con.prepareStatement(

					"SELECT r.precio "

					+ "FROM tickets t "

					+ "JOIN viajes v ON t.idViaje = v.idViaje "

					+ "JOIN recorridos r ON v.idRecorrido = r.idRecorrido "

					+ "WHERE t.idTicket = ?");

			st.setInt(1, billeteId);

			rs = st.executeQuery();

			

			if(!rs.next()) {

				throw new SQLException("No se pudo obtener el precio para una plaza de dicho viaje");

			}

			

			//Guardamos el valor del precio en una variable que vamos a usar a continuación

			int precioPorPlaza = rs.getInt("precio");

			

			//Calculamos el valor total del billete modificado

			int precioTotal = precioPorPlaza * nuevoNroPlazas;

			

			

			//Modificamos el billete, cambiando su cantidad y su precio total

			st = con.prepareStatement(

					"UPDATE tickets SET cantidad = ?, precio = ? "

					+ "WHERE idTicket = ?");

			st.setInt(1 ,nuevoNroPlazas);

			st.setInt(2, precioTotal);

			st.setInt(3, billeteId);

			st.executeUpdate();

			

			//Comiteamos la transaccion

			con.commit();

			

		}catch (CompraBilleteTrenException e) {

			

			LOGGER.error("Error al modificar el billete: " + e.getMessage());

			if (con != null) {

				con.rollback(); // Realizamos el rollback de la transacción

			}	

			throw e;

			

		}catch (SQLException e){

		

			LOGGER.error("Error al modificar el billete: " + e.getMessage());

			if (con != null) {

				con.rollback(); // Realizamos el rollback de la transacción

			}

			throw e;

		

		//Cerramos los recursos utilizados

		}finally {

			if (rs != null) {

				rs.close();

			}

			if (st != null) {

				st.close();

			}

			if (con != null) {

				con.close();

			}

		}

		

	}

}

