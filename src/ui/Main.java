package ui;

import bll.*;
import dll.ControllerAdministrador;
import dll.ControllerMedico;
import dll.ControllerPaciente;
import dll.ControllerUsuario;

import javax.swing.*;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAdjusters;

public class Main {

	// acepta el ingreso de fechas: dd/mm/yyyy
	private static final DateTimeFormatter F_DDMMYYYY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);

	// acepta el ingreso de horas: hh:mm
	private static final DateTimeFormatter F_HHMM = DateTimeFormatter.ofPattern("HH:mm")
			.withResolverStyle(ResolverStyle.STRICT);

	// Valida y parsea el formato de hora hh:mm
	private static LocalTime parseHoraHHmm(String texto) {
		String s = texto == null ? "" : texto.trim();
		if (!s.matches("^\\d{2}:\\d{2}$")) {
			throw new IllegalArgumentException("Ingrese una hora válida 00:00");
		}
		return LocalTime.parse(s, F_HHMM);
	}

	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null, " HealthHub - Sistema de turnos ");

		// login
		ControllerUsuario cu = new ControllerUsuario();
		Usuario u = null;

		while (u == null) {

			String user = null;
			while (true) {
				user = JOptionPane.showInputDialog(null, "Usuario:");
				if (user == null)
					return;
				user = user.trim();
				if (user.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Debe ingresar un nombre de usuario");
					continue;
				}

				try {
					if (!cu.existeUsuario(user)) {
						JOptionPane.showMessageDialog(null, "El usuario no existe intente nuevamente");
						continue;
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error verificando usuario: " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
					continue;
				}
				break;
			}

			while (u == null) {
				String pass = JOptionPane.showInputDialog(null, "Contraseña:");
				if (pass == null)
					return;
				pass = pass.trim();
				if (pass.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Debe ingresar una contraseña");
					continue;
				}

				try {
					var opt = cu.login(user, pass);
					if (opt.isEmpty()) {
						JOptionPane.showMessageDialog(null, "Contraseña incorrecta intente nuevamente");
						continue;
					}
					u = opt.get();
				} catch (IllegalStateException ise) {

					JOptionPane.showMessageDialog(null, ise.getMessage());

					break;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error en el login: " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);

				}
			}
		}

		JOptionPane.showMessageDialog(null, "Bienvenido " + u.getNombre());

		if (u instanceof Paciente p) {
			ControllerPaciente cp = new ControllerPaciente(p);

			String[] ops = { "Buscar médicos por especialidad", "Ver disponibilidad de un médico", "Reservar turno",
					"Ver mis turnos activos", "Cancelar turno", "Confirmar asistencia", "Agregar a favoritos",
					"Ver favoritos", "Ver recomendaciones", "Salir" };

			boolean salir = false;
			while (!salir) {
				Object choice = JOptionPane.showInputDialog(null, "Menú Paciente", "Paciente",
						JOptionPane.PLAIN_MESSAGE, null, ops, ops[0]);

				if (choice == null || "Salir".equals(choice.toString())) {
					salir = true;
					continue;
				}
				String op = choice.toString();

				try {
					switch (op) {
					case "Buscar médicos por especialidad" -> {
						String esp = JOptionPane.showInputDialog(null, "Especialidad (ej: Clínica Médica):");
						if (esp == null)
							continue;
						var meds = cp.filtrarPorEspecialidad(esp.trim());
						if (meds.isEmpty()) {
							JOptionPane.showMessageDialog(null, "No se encontraron médicos");
						} else {
							StringBuilder sb = new StringBuilder("Médicos:\n");
							meds.forEach(m -> sb.append("- ").append(m.getUsuario()).append(" | ").append(m.getNombre())
									.append(" ").append(m.getApellido()).append(" | ").append(m.getEspecialidad())
									.append("\n"));
							JTextArea ta = new JTextArea(sb.toString());
							ta.setEditable(false);
							JOptionPane.showMessageDialog(null, new JScrollPane(ta));
						}
					}
					case "Ver disponibilidad de un médico" -> {
						String userMed = JOptionPane.showInputDialog(null, "Usuario del médico:");
						if (userMed == null)
							continue;
						var franjas = cp.obtenerHorariosDisponibles(userMed.trim());
						if (franjas.isEmpty()) {
							JOptionPane.showMessageDialog(null, "Sin disponibilidad");
						} else {
							StringBuilder sb = new StringBuilder("Disponibilidad:\n");
							for (String f : franjas)
								sb.append("• ").append(f).append("\n");
							JTextArea ta = new JTextArea(sb.toString());
							ta.setEditable(false);
							JOptionPane.showMessageDialog(null, new JScrollPane(ta));
						}
					}
					case "Reservar turno" -> {
						String userMed = JOptionPane.showInputDialog(null, "Usuario del médico:");
						if (userMed == null)
							continue;
						String sFecha = JOptionPane.showInputDialog(null, "Fecha (YYYY-MM-DD):");
						if (sFecha == null)
							continue;
						String sHora = JOptionPane.showInputDialog(null, "Hora hh:mm");
						if (sHora == null)
							continue;

						LocalDate fecha;
						try {
							fecha = LocalDate.parse(sFecha.trim(), DateTimeFormatter.ISO_DATE);
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Fecha inválida");
							continue;
						}

						LocalTime hora;
						try {
							hora = parseHoraHHmm(sHora);
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, e.getMessage());
							continue;
						}

						var zdt = fecha.atTime(hora).atZone(java.time.ZoneId.systemDefault());
						Date cuando = Date.from(zdt.toInstant());
						long id = cp.solicitarTurno(userMed.trim(), cuando);
						JOptionPane.showMessageDialog(null, "Turno reservado nro = " + id);
					}
					case "Ver mis turnos activos" -> {
						var list = cp.turnosActivos();
						if (list.isEmpty()) {
							JOptionPane.showMessageDialog(null, "No tienes turnos activos");
						} else {
							StringBuilder sb = new StringBuilder("Turnos activos:\n");
							list.forEach(t -> sb.append("- ").append(t.getFechaHora()).append(" | estado: ")
									.append(t.getEstado()).append("\n"));
							JTextArea ta = new JTextArea(sb.toString());
							ta.setEditable(false);
							JOptionPane.showMessageDialog(null, new JScrollPane(ta));
						}
					}
					case "Cancelar turno" -> {
						String sId = JOptionPane.showInputDialog(null, "Nro de turno a cancelar:");
						if (sId == null)
							continue;
						try {
							cp.cancelarTurno(Long.parseLong(sId.trim()));
							JOptionPane.showMessageDialog(null, "Turno cancelado.");
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Número inválido");
						}
					}
					case "Confirmar asistencia" -> {
						String sId = JOptionPane.showInputDialog(null, "Nro de turno a confirmar asistencia:");
						if (sId == null)
							continue;
						try {
							cp.confirmarAsistencia(Long.parseLong(sId.trim()));
							JOptionPane.showMessageDialog(null, "Asistencia confirmada.");
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Número inválido");
						}
					}
					case "Agregar a favoritos" -> {
						String medUser = JOptionPane.showInputDialog(null, "Usuario del médico:");
						if (medUser == null)
							continue;
						cp.agregarAFavoritos(medUser.trim());
						JOptionPane.showMessageDialog(null, "Agregado a favoritos");
					}
					case "Ver favoritos" -> {
						var favs = cp.verFavoritos();
						if (favs.isEmpty()) {
							JOptionPane.showMessageDialog(null, "Sin favoritos");
						} else {
							StringBuilder sb = new StringBuilder("Favoritos:\n");
							favs.forEach(m -> sb.append("- ").append(m.getNombre()).append(" ").append(m.getApellido())
									.append(" (").append(m.getEspecialidad()).append(")\n"));
							JTextArea ta = new JTextArea(sb.toString());
							ta.setEditable(false);
							JOptionPane.showMessageDialog(null, new JScrollPane(ta));
						}
					}
					case "Ver recomendaciones" -> {
						var recs = cp.mostrarRecomendaciones();
						StringBuilder sb = new StringBuilder("Recomendaciones:\n");
						for (String r : recs)
							sb.append("• ").append(r).append("\n");
						JTextArea ta = new JTextArea(sb.toString());
						ta.setEditable(false);
						JOptionPane.showMessageDialog(null, new JScrollPane(ta));
					}
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}

		} else if (u instanceof Medico m) {
			ControllerMedico cm = new ControllerMedico(m);

			String[] ops = { "Registrar disponibilidad", "Modificar disponibilidad", "Eliminar disponibilidad",
					"Visualizar agenda", "Confirmar asistencia", "Cancelar turno", "Reprogramar turno",
					"Registrar consulta", "Salir" };

			boolean salir = false;
			while (!salir) {
				Object choice = JOptionPane.showInputDialog(null, "Menú Médico", "Médico", JOptionPane.PLAIN_MESSAGE,
						null, ops, ops[0]);

				if (choice == null || "Salir".equals(choice.toString())) {
					salir = true;
					continue;
				}

				String op = choice.toString();

				try {
					switch (op) {

					case "Registrar disponibilidad" -> {
						// selecciona dia de la semana
						String[] dias = { "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO" };
						String sDia = (String) JOptionPane.showInputDialog(null,
								"Seleccione el día de la semana para la repetición:", "Día Semanal",
								JOptionPane.QUESTION_MESSAGE, null, dias, dias[0]);
						if (sDia == null)
							break;

						String diaIngles = switch (sDia) {
						case "LUNES" -> "MONDAY";
						case "MARTES" -> "TUESDAY";
						case "MIÉRCOLES" -> "WEDNESDAY";
						case "JUEVES" -> "THURSDAY";
						case "VIERNES" -> "FRIDAY";
						case "SÁBADO" -> "SATURDAY";
						case "DOMINGO" -> "SUNDAY";
						default -> throw new IllegalStateException("Día de la semana desconocido: " + sDia);
						};
						DayOfWeek diaEnum = DayOfWeek.valueOf(diaIngles);

						// seleccina hasta que fecha repetir la disponibilidad
						LocalDate fechaFin = null;
						while (true) {
							String s = JOptionPane.showInputDialog(null, "Repetir hasta (dd/mm/aaaa):");
							if (s == null) {
								return;
							}
							s = s.trim();
							if (!s.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
								JOptionPane.showMessageDialog(null, "Formato inválido ingrese ej: 25/12/2025");
								continue;
							}
							try {
								fechaFin = LocalDate.parse(s, F_DDMMYYYY);
								if (fechaFin.isBefore(LocalDate.now())) {
									JOptionPane.showMessageDialog(null, "La fecha debe ser futura");
									continue;
								}
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Fecha inválida");
							}
						}

						LocalTime hi = null;
						while (true) {
							String sHoraInicio = JOptionPane.showInputDialog(null, "Hora inicio (hh:mm):");
							if (sHoraInicio == null)
								return;
							try {
								hi = parseHoraHHmm(sHoraInicio);
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, e.getMessage());
							}
						}

						LocalTime hf = null;
						while (true) {
							String sHoraFin = JOptionPane.showInputDialog(null, "Hora fin (hh:mm):");
							if (sHoraFin == null)
								return;
							try {
								hf = parseHoraHHmm(sHoraFin);
								if (!hf.isAfter(hi)) {
									JOptionPane.showMessageDialog(null,
											"La hora fin debe ser mayor que la hora inicio");
									continue;
								}
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, e.getMessage());
							}
						}

						LocalDate fechaRegistro = LocalDate.now().with(TemporalAdjusters.nextOrSame(diaEnum));
						int registros = 0;

						while (!fechaRegistro.isAfter(fechaFin)) {
							try {
								cm.registrarDisponibilidad(java.sql.Date.valueOf(fechaRegistro),
										java.sql.Time.valueOf(hi), java.sql.Time.valueOf(hf));
								registros++;
							} catch (IllegalArgumentException ex) {
								System.out.println(
										"Solape en " + fechaRegistro.format(F_DDMMYYYY) + ": " + ex.getMessage());
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, "Error al registrar "
										+ fechaRegistro.format(F_DDMMYYYY) + ": " + ex.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
								break;
							}
							fechaRegistro = fechaRegistro.plusWeeks(1);
						}

						if (registros > 0) {
							JOptionPane.showMessageDialog(null, "Disponibilidad semanal registrada");
						} else {
							JOptionPane.showMessageDialog(null,
									"Ya tiene agenda creada para esa fecha, revise los datos ingresados");
						}
					}

					case "Modificar disponibilidad" -> {
						Integer id = null;
						while (true) {
							String sId = JOptionPane.showInputDialog(null, "Nro disponibilidad:");
							if (sId == null) {
								break;
							}
							sId = sId.trim();
							if (!sId.matches("\\d+")) {
								JOptionPane.showMessageDialog(null, "Id inválido");
								continue;
							}
							int tmp = Integer.parseInt(sId);
							try {
								cm.validarId(tmp);
								id = tmp;
								break;
							} catch (IllegalStateException ex) {
								JOptionPane.showMessageDialog(null, ex.getMessage());
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, "Error validando id: " + ex.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
								break;
							}
						}
						if (id == null)
							break;

						// ingresar hora
						LocalTime hi;
						while (true) {
							String s = JOptionPane.showInputDialog(null, "Nueva hora inicio (hh:mm):");
							if (s == null) {
								hi = null;
								break;
							}
							try {
								hi = parseHoraHHmm(s);
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, e.getMessage());
							}
						}
						if (hi == null)
							break;

						// ingresar hora fin
						LocalTime hf;
						while (true) {
							String s = JOptionPane.showInputDialog(null, "Nueva hora fin (hh:mm):");
							if (s == null) {
								hf = null;
								break;
							}
							try {
								hf = parseHoraHHmm(s);
								if (!hf.isAfter(hi)) {
									JOptionPane.showMessageDialog(null,
											"La hora fin debe ser mayor que la hora inicio");
									continue;
								}
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, e.getMessage());
							}
						}
						if (hf == null)
							break;

						cm.modificarDisponibilidad(id, Time.valueOf(hi), Time.valueOf(hf));
						JOptionPane.showMessageDialog(null, "Disponibilidad modificada");
					}

					case "Eliminar disponibilidad" -> {
						Integer id = null;

						// valida el id antes de ingresar la hora
						while (true) {
							String sId = JOptionPane.showInputDialog(null, "Nro de disponibilidad a eliminar:");
							if (sId == null)
								break;
							sId = sId.trim();

							if (!sId.matches("\\d+")) {
								JOptionPane.showMessageDialog(null, "Id inválido");
								continue;
							}

							int tmp = Integer.parseInt(sId);
							try {
								cm.validarId(tmp);
								id = tmp;
								break;
							} catch (IllegalStateException ex) {
								JOptionPane.showMessageDialog(null, ex.getMessage());
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, "Error al validar ID: " + ex.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
								break;
							}
						}

						if (id == null)
							break;

						int confirm = JOptionPane.showConfirmDialog(null,
								"¿Está seguro de eliminar la disponibilidad nro " + id + "?", "Confirmar eliminación",
								JOptionPane.YES_NO_OPTION);

						if (confirm != JOptionPane.YES_OPTION)
							break;

						try {
							cm.eliminarDisponibilidad(id);
							JOptionPane.showMessageDialog(null, "Disponibilidad eliminada");
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Error al eliminar disponibilidad: " + e.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}

					case "Visualizar agenda" -> {
						LocalDate d1 = null, d2 = null;

						while (true) {
							String s = JOptionPane.showInputDialog(null, "Desde (dd/mm/aaaa):");
							if (s == null)
								break;
							s = s.trim();
							if (!s.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
								JOptionPane.showMessageDialog(null, "Formato inválido ingrese ej: 01/11/2025");
								continue;
							}
							try {
								d1 = LocalDate.parse(s, F_DDMMYYYY);
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Fecha inválida");
							}
						}
						if (d1 == null)
							break;

						while (true) {
							String s = JOptionPane.showInputDialog(null, "Hasta (dd/mm/aaaa):");
							if (s == null) {
								d1 = null;
								break;
							}
							s = s.trim();
							if (!s.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
								JOptionPane.showMessageDialog(null, "Formato inválido ingrese ej: 30/11/2025");
								continue;
							}
							try {
								d2 = LocalDate.parse(s, F_DDMMYYYY);
								if (d2.isBefore(d1)) {
									JOptionPane.showMessageDialog(null,
											"La fecha hasta no puede ser anterior a la fecha desde");
									continue;
								}
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Fecha inválida");
							}
						}
						if (d2 == null)
							break;

						int inc = JOptionPane.showConfirmDialog(null, "¿Incluir turnos cancelados?", "Filtro de estado",
								JOptionPane.YES_NO_OPTION);
						boolean incluirCancelados = (inc == JOptionPane.YES_OPTION);
						var filas = cm.visualizarAgenda(java.sql.Date.valueOf(d1), java.sql.Date.valueOf(d2),
								incluirCancelados);

						if (filas.isEmpty()) {
							JOptionPane.showMessageDialog(null, "No hay turnos agendados"
									+ (incluirCancelados ? "" : ""));
						} else {
							StringBuilder sb = new StringBuilder("Agenda:\n");
							for (String f : filas)
								sb.append("- ").append(f).append("\n");
							JTextArea ta = new JTextArea(sb.toString());
							ta.setEditable(false);
							JOptionPane.showMessageDialog(null, new JScrollPane(ta));
						}
					}

					case "Confirmar asistencia" -> {
						Long idTurno = null;
						while (true) {
							String sId = JOptionPane.showInputDialog(null, "Nro de turno a confirmar:");
							if (sId == null)
								break;
							sId = sId.trim();

							if (sId.isEmpty() || !sId.matches("\\d+")) {
								JOptionPane.showMessageDialog(null, "Id inválido");
								continue;
							}

							long tmp = Long.parseLong(sId);
							try {
								if (!cm.validarIdTurno(tmp)) {
									JOptionPane.showMessageDialog(null, "Turno inexistente");
									continue;
								}
								idTurno = tmp;
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Error validando turno: " + e.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
								break;
							}
						}

						if (idTurno == null)
							break;

						try {
							cm.confirmarAsistencia(idTurno);
							JOptionPane.showMessageDialog(null, "Asistencia confirmada");
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Error al confirmar: " + e.getMessage(), "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}

					case "Cancelar turno" -> {
						Long idTurno = null;

						while (true) {
							String sId = JOptionPane.showInputDialog(null, "Nro de turno a cancelar:");
							if (sId == null)
								break;
							sId = sId.trim();

							if (sId.isEmpty() || !sId.matches("\\d+")) {
								JOptionPane.showMessageDialog(null, "Id inválido");
								continue;
							}

							long tmp = Long.parseLong(sId);
							try {
								if (!cm.validarIdTurno(tmp)) {
									JOptionPane.showMessageDialog(null, "Turno inexistente");
									continue;
								}
								idTurno = tmp;
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Error validando turno: " + e.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
								break;
							}
						}

						if (idTurno == null)
							break;

						try {
							cm.cancelarTurno(idTurno);
							JOptionPane.showMessageDialog(null, "Turno cancelado");
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Error al cancelar: " + e.getMessage(), "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}

					case "Reprogramar turno" -> {
						Long idTurno = null;
						while (true) {
							String sId = JOptionPane.showInputDialog(null, "Nro de turno a reprogramar:");
							if (sId == null)
								break;
							sId = sId.trim();

							if (!sId.matches("\\d+")) {
								JOptionPane.showMessageDialog(null, "Id inválido");
								continue;
							}

							long tmp = Long.parseLong(sId);
							try {
								if (!cm.validarIdTurno(tmp)) {
									JOptionPane.showMessageDialog(null, "Turno inexistente");
									continue;
								}
								idTurno = tmp;
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Error validando turno: " + e.getMessage());
							}
						}

						if (idTurno == null)
							break;

						LocalDate fecha = null;
						while (true) {
							String s = JOptionPane.showInputDialog(null, "Nueva fecha (dd/mm/aaaa):");
							if (s == null)
								break;
							s = s.trim();

							if (!s.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
								JOptionPane.showMessageDialog(null, "Formato inválido ingrese ej: 25/12/2025");
								continue;
							}

							try {
								fecha = LocalDate.parse(s, F_DDMMYYYY);
								if (fecha.isBefore(LocalDate.now())) {
									JOptionPane.showMessageDialog(null, "La fecha debe ser futura");
									continue;
								}
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Fecha inválida");
							}
						}
						if (fecha == null)
							break;

						LocalTime hora = null;
						while (true) {
							String sHora = JOptionPane.showInputDialog(null, "Nueva hora (hh:mm):");
							if (sHora == null)
								break;
							try {
								hora = parseHoraHHmm(sHora.trim());
								break;
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, e.getMessage());
							}
						}
						if (hora == null)
							break;

						int confirm = JOptionPane
								.showConfirmDialog(null,
										"¿Desea reprogramar el turno nro " + idTurno + " a " + fecha.format(F_DDMMYYYY)
												+ " " + hora + "?",
										"Confirmar reprogramación", JOptionPane.YES_NO_OPTION);
						if (confirm != JOptionPane.YES_OPTION)
							break;

						try {
							Date nueva = Date
									.from(fecha.atTime(hora).atZone(java.time.ZoneId.systemDefault()).toInstant());
							cm.reprogramarTurno(idTurno, nueva);
							JOptionPane.showMessageDialog(null, "Turno reprogramado");
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Error al reprogramar: " + e.getMessage(), "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}

					case "Registrar consulta" -> {
						String userPac;
						while (true) {
							userPac = JOptionPane.showInputDialog(null, "Usuario del paciente:");
							if (userPac == null)
								break;
							userPac = userPac.trim();
							if (userPac.isEmpty()) {
								JOptionPane.showMessageDialog(null, "Ingrese un usuario válido");
								continue;
							}
							if (!cm.validarPaciente(userPac)) {
								JOptionPane.showMessageDialog(null, "Paciente inexistente");
								continue;
							}
							break;
						}
						if (userPac == null)
							break;

						String motivo = JOptionPane.showInputDialog(null, "Motivo:");
						if (motivo == null || motivo.isBlank()) {
							JOptionPane.showMessageDialog(null, "Ingrese el motivo");
							break;
						}
						String diag = JOptionPane.showInputDialog(null, "Diagnóstico:");
						if (diag == null)
							break;
						String trat = JOptionPane.showInputDialog(null, "Tratamiento:");
						if (trat == null)
							break;
						String seg = JOptionPane.showInputDialog(null, "Recomendaciones:");
						if (seg == null)
							break;

						Consulta c = Consulta.hoy(motivo.trim(), diag.trim(), trat.trim(), seg.trim());
						long id = cm.registrarConsulta(userPac, c);
						JOptionPane.showMessageDialog(null, "Consulta nro " + id + " registrada");
					}
					}

				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}

		} else if (u instanceof Administrador a) {
			ControllerAdministrador ca = new ControllerAdministrador(a);
			String[] ops = { "Alta de paciente", "Modificar datos de paciente", "Alta de médico",
					"Modificar datos de médico", "Listar usuarios", "Eliminar usuario", "Salir" };

			boolean salir = false;
			while (!salir) {
				Object choice = JOptionPane.showInputDialog(null, "Menú Administrador", "Administrador",
						JOptionPane.PLAIN_MESSAGE, null, ops, ops[0]);

				if (choice == null || "Salir".equals(choice.toString())) {
					salir = true;
					continue;
				}
				String op = choice.toString();

				try {
					switch (op) {
					// ALTA PACIENTE ----------------
					case "Alta de paciente" -> {
						String usr, nom, ape, pass, nro, os;

						// Usuario
						while (true) {
							usr = JOptionPane.showInputDialog("Usuario:");
							if (usr == null)
								break;
							if (!usr.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el usuario.");
						}
						if (usr == null)
							continue;

						// Nombre
						while (true) {
							nom = JOptionPane.showInputDialog("Nombre:");
							if (nom == null)
								break;
							if (!nom.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el nombre.");
						}
						if (nom == null)
							continue;

						// Apellido
						while (true) {
							ape = JOptionPane.showInputDialog("Apellido:");
							if (ape == null)
								break;
							if (!ape.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el apellido.");
						}
						if (ape == null)
							continue;

						// Contraseña
						while (true) {
							pass = JOptionPane.showInputDialog("Contraseña:");
							if (pass == null)
								break;
							if (!pass.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese la contraseña.");
						}
						if (pass == null)
							continue;

						// contrato
						while (true) {
							nro = JOptionPane.showInputDialog("Número de contrato:");
							if (nro == null)
								break;
							if (!nro.trim().isEmpty() && nro.matches("\\d+"))
								break;
							JOptionPane.showMessageDialog(null, "Ingrese un número de contrato válido.");
						}
						if (nro == null)
							continue;

						// Obra social
						while (true) {
							os = JOptionPane.showInputDialog("Obra social:");
							if (os == null)
								break;
							if (!os.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese la obra social.");
						}
						if (os == null)
							continue;

						ca.registrarPaciente(usr.trim(), nom.trim(), ape.trim(), pass.trim(),
								Integer.parseInt(nro.trim()), os.trim());
						JOptionPane.showMessageDialog(null, "Paciente registrado exitosamente");
					}

					// MODIFICAR PACIENTE ----------------
					case "Modificar datos de paciente" -> {
						String usr, nom, ape, pass, nro, os;

						// Usr a modificar
						while (true) {
							usr = JOptionPane.showInputDialog("Usuario del paciente a modificar:");
							if (usr == null)
								break;
							if (!usr.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el usuario del paciente.");
						}
						if (usr == null)
							continue;

						// Nombre
						while (true) {
							nom = JOptionPane.showInputDialog("Nuevo nombre:");
							if (nom == null)
								break;
							if (!nom.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el nuevo nombre.");
						}
						if (nom == null)
							continue;

						// Apellido
						while (true) {
							ape = JOptionPane.showInputDialog("Nuevo apellido:");
							if (ape == null)
								break;
							if (!ape.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el nuevo apellido.");
						}
						if (ape == null)
							continue;

						// Contraseña
						while (true) {
							pass = JOptionPane.showInputDialog("Nueva contraseña:");
							if (pass == null)
								break;
							if (!pass.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese la nueva contraseña.");
						}
						if (pass == null)
							continue;

						// Número de contrato
						while (true) {
							nro = JOptionPane.showInputDialog("Nuevo número de contrato:");
							if (nro == null)
								break;
							if (!nro.trim().isEmpty() && nro.matches("\\d+"))
								break;
							JOptionPane.showMessageDialog(null, "Ingrese un número de contrato válido.");
						}
						if (nro == null)
							continue;

						// Obra social
						while (true) {
							os = JOptionPane.showInputDialog("Nueva obra social:");
							if (os == null)
								break;
							if (!os.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese la nueva obra social.");
						}
						if (os == null)
							continue;

						ca.modificarPaciente(usr.trim(), nom.trim(), ape.trim(), pass.trim(),
								Integer.parseInt(nro.trim()), os.trim());
						JOptionPane.showMessageDialog(null, "Paciente modificado exitosamente");
					}

					// ALTA MÉDICO ----------------
					case "Alta de médico" -> {
						String usr, nom, ape, pass, matricula, esp;

						while (true) {
							usr = JOptionPane.showInputDialog("Usuario:");
							if (usr == null)
								break;
							if (!usr.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el usuario.");
						}
						if (usr == null)
							continue;

						while (true) {
							nom = JOptionPane.showInputDialog("Nombre:");
							if (nom == null)
								break;
							if (!nom.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el nombre.");
						}
						if (nom == null)
							continue;

						while (true) {
							ape = JOptionPane.showInputDialog("Apellido:");
							if (ape == null)
								break;
							if (!ape.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el apellido.");
						}
						if (ape == null)
							continue;

						while (true) {
							pass = JOptionPane.showInputDialog("Contraseña:");
							if (pass == null)
								break;
							if (!pass.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese la contraseña.");
						}
						if (pass == null)
							continue;

						while (true) {
							matricula = JOptionPane.showInputDialog("Matrícula:");
							if (matricula == null)
								break;
							if (!matricula.trim().isEmpty() && matricula.matches("\\d+"))
								break;
							JOptionPane.showMessageDialog(null, "Ingrese una matrícula válida (solo números).");
						}
						if (matricula == null)
							continue;

						while (true) {
							esp = JOptionPane.showInputDialog("Especialidad:");
							if (esp == null)
								break;
							if (!esp.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese la especialidad.");
						}
						if (esp == null)
							continue;

						ca.registrarMedico(usr.trim(), nom.trim(), ape.trim(), pass.trim(), matricula.trim(),
								esp.trim());
						JOptionPane.showMessageDialog(null, "Médico registrado exitosamente");
					}

					// MODIFICAR MÉDICO ----------------
					case "Modificar datos de médico" -> {
						String usr, nom, ape, pass, matricula, esp;

						while (true) {
							usr = JOptionPane.showInputDialog("Usuario del médico a modificar:");
							if (usr == null)
								break;
							if (!usr.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el usuario del médico.");
						}
						if (usr == null)
							continue;

						while (true) {
							nom = JOptionPane.showInputDialog("Nuevo nombre:");
							if (nom == null)
								break;
							if (!nom.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el nuevo nombre.");
						}
						if (nom == null)
							continue;

						while (true) {
							ape = JOptionPane.showInputDialog("Nuevo apellido:");
							if (ape == null)
								break;
							if (!ape.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el nuevo apellido.");
						}
						if (ape == null)
							continue;

						while (true) {
							pass = JOptionPane.showInputDialog("Nueva contraseña:");
							if (pass == null)
								break;
							if (!pass.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese la nueva contraseña.");
						}
						if (pass == null)
							continue;

						while (true) {
							matricula = JOptionPane.showInputDialog("Nueva matrícula:");
							if (matricula == null)
								break;
							if (!matricula.trim().isEmpty() && matricula.matches("\\d+"))
								break;
							JOptionPane.showMessageDialog(null, "Ingrese una matrícula válida (solo números).");
						}
						if (matricula == null)
							continue;

						while (true) {
							esp = JOptionPane.showInputDialog("Nueva especialidad:");
							if (esp == null)
								break;
							if (!esp.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese la nueva especialidad.");
						}
						if (esp == null)
							continue;

						ca.modificarMedico(usr.trim(), nom.trim(), ape.trim(), pass.trim(), matricula.trim(),
								esp.trim());
						JOptionPane.showMessageDialog(null, "Médico modificado exitosamente");
					}

					// LISTAR USUARIOS ----------------
					case "Listar usuarios" -> {
						String rol = JOptionPane.showInputDialog("Ingrese rol a listar (Paciente o Medico):");
						if (rol == null)
							continue;
						var usuarios = ca.listarUsuariosPorRol(rol.trim());
						if (usuarios.isEmpty()) {
							JOptionPane.showMessageDialog(null, "No hay usuarios registrados con ese rol.");
						} else {
							StringBuilder sb = new StringBuilder("Usuarios:\n");
							usuarios.forEach(u1 -> sb.append("- ").append(u1).append("\n"));
							JTextArea ta = new JTextArea(sb.toString());
							ta.setEditable(false);
							JOptionPane.showMessageDialog(null, new JScrollPane(ta));
						}
					}

					// ELIMINAR USUARIO ----------------
					case "Eliminar usuario" -> {
						String usr;
						while (true) {
							usr = JOptionPane.showInputDialog("Usuario a eliminar:");
							if (usr == null)
								break;
							if (!usr.trim().isEmpty())
								break;
							JOptionPane.showMessageDialog(null, "Ingrese el usuario a eliminar.");
						}
						if (usr == null)
							continue;

						ca.eliminarUsuario(usr.trim());
						JOptionPane.showMessageDialog(null, "Usuario eliminado exitosamente");
					}
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		JOptionPane.showMessageDialog(null, "Gracias por usar HealthHub!");
	}
}
