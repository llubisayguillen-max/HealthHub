package ui;

import bll.*;
import dll.ControllerAdministrador;
import dll.ControllerMedico;
import dll.ControllerPaciente;
import dll.ControllerUsuario;

import javax.swing.*;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

public class Main {

	// acepta el ingreso de horas: 12:00 12:15 ...
	private static LocalTime parseHoraFlexible(String texto) {
		String s = texto == null ? "" : texto.trim().toUpperCase().replace(".", "");
		DateTimeFormatter[] fmts = new DateTimeFormatter[] { DateTimeFormatter.ofPattern("H:mm"),
				DateTimeFormatter.ofPattern("HH:mm"), DateTimeFormatter.ofPattern("h:mm a"),
				DateTimeFormatter.ofPattern("hh:mm a"), DateTimeFormatter.ofPattern("H"),
				DateTimeFormatter.ofPattern("h a") };
		for (DateTimeFormatter f : fmts) {
			try {
				return LocalTime.parse(s, f);
			} catch (Exception ignore) {
			}
		}
		throw new IllegalArgumentException("Formato de hora inválido. Usá 24h (ej: 09:30, 12:00)");
	}

	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null, " HealthHub - Sistema de turnos ");

		// login
		ControllerUsuario cu = new ControllerUsuario();
		Usuario u = null;
		while (u == null) {
			String user = JOptionPane.showInputDialog(null, "Usuario:");
			if (user == null)
				return;
			String pass = JOptionPane.showInputDialog(null, "Contraseña:");
			if (pass == null)
				return;

			Optional<Usuario> opt = cu.login(user.trim(), pass.trim());
			if (opt == null || opt.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Credenciales inválidas intente nuevamente");
			} else {
				u = opt.get();
			}
		}
		JOptionPane.showMessageDialog(null, "Bienvenido " + u.getNombre());

		// menu por rol
		if (u instanceof Paciente p) {
			ControllerPaciente cp = new ControllerPaciente(p);

			String[] ops = { "Buscar médicos por especialidad", "Ver disponibilidad de un médico", "Reservar turno",
					"Ver mis turnos activos", "Cancelar turno", "Confirmar asistencia", "Agregar a favoritos",
					"Ver favoritos", "Ver recomendaciones", "Salir" };

			Object choice = JOptionPane.showInputDialog(null, "Menú Paciente", "Paciente", JOptionPane.PLAIN_MESSAGE,
					null, ops, ops[0]);
			if (choice == null || "Salir".equals(choice)) {
				JOptionPane.showMessageDialog(null, "Gracias por usar HealthHub!");
				return;
			}
			String op = choice.toString();

			try {
				if (op.equals(ops[0])) { // buscar medicos
					String esp = JOptionPane.showInputDialog(null, "Especialidad (ej: Clínica Médica):");
					if (esp != null) {
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
				} else if (op.equals(ops[1])) { // ver disponibilidad
					String userMed = JOptionPane.showInputDialog(null, "Nombre del médico:");
					if (userMed != null) {
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
				} else if (op.equals(ops[2])) { // reservar turno
					String userMed = JOptionPane.showInputDialog(null, "Nombre del médico:");
					if (userMed == null)
						return;
					String sFecha = JOptionPane.showInputDialog(null, "Fecha (YYYY-MM-DD):");
					if (sFecha == null)
						return;
					String sHora = JOptionPane.showInputDialog(null, "Hora (hh:mm AM/PM):");
					if (sHora == null)
						return;

					LocalDate fecha;
					try {
						fecha = LocalDate.parse(sFecha.trim(), DateTimeFormatter.ISO_DATE);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Fecha inválida");
						return;
					}

					LocalTime hora;
					try {
						hora = parseHoraFlexible(sHora);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						return;
					}

					var zdt = fecha.atTime(hora).atZone(java.time.ZoneId.systemDefault());
					Date cuando = Date.from(zdt.toInstant());
					long id = cp.solicitarTurno(userMed.trim(), cuando);
					JOptionPane.showMessageDialog(null, "Turno reservado nro = " + id);
				} else if (op.equals(ops[3])) { // ver turnos activos
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
				} else if (op.equals(ops[4])) { // cancelar turno
					String sId = JOptionPane.showInputDialog(null, "Nro de turno a cancelar:");
					if (sId != null) {
						try {
							cp.cancelarTurno(Long.parseLong(sId.trim()));
							JOptionPane.showMessageDialog(null, "Turno cancelado.");
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Número inválido");
						}
					}
				} else if (op.equals(ops[5])) { // confirmar asistencia
					String sId = JOptionPane.showInputDialog(null, "Nro de turno a confirmar asistencia:");
					if (sId != null) {
						try {
							cp.confirmarAsistencia(Long.parseLong(sId.trim()));
							JOptionPane.showMessageDialog(null, "Asistencia confirmada.");
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Número inválido");
						}
					}
				} else if (op.equals(ops[6])) { // agregar a favoritos
					String medUser = JOptionPane.showInputDialog(null, "Nombre del médico:");
					if (medUser != null) {
						cp.agregarAFavoritos(medUser.trim());
						JOptionPane.showMessageDialog(null, "Agregado a favoritos.");
					}
				} else if (op.equals(ops[7])) { // ver favoritos
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
				} else if (op.equals(ops[8])) { // recomendaciones
					var recs = cp.mostrarRecomendaciones();
					StringBuilder sb = new StringBuilder("Recomendaciones:\n");
					for (String r : recs)
						sb.append("• ").append(r).append("\n");
					JTextArea ta = new JTextArea(sb.toString());
					ta.setEditable(false);
					JOptionPane.showMessageDialog(null, new JScrollPane(ta));
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}

		} else if (u instanceof Medico m) {
			ControllerMedico cm = new ControllerMedico(m);

			String[] ops = { "Registrar disponibilidad", "Modificar disponibilidad", "Listar disponibilidad",
					"Eliminar disponibilidad", "Visualizar agenda", "Próximos turnos", "Turnos del día",
					"Confirmar asistencia", "Cancelar turno", "Reprogramar turno", "Registrar consulta", "Salir" };

			Object choice = JOptionPane.showInputDialog(null, "Menú Médico", "Médico", JOptionPane.PLAIN_MESSAGE, null,
					ops, ops[0]);
			if (choice == null || "Salir".equals(choice)) {
				JOptionPane.showMessageDialog(null, "Gracias por usar HealthHub!");
				return;
			}
			String op = choice.toString();

			try {
				if (op.equals(ops[0])) { // registrar disponibilidad
					String sHi = JOptionPane.showInputDialog(null, "Hora inicio (HH:mm o hh:mm AM/PM):");
					if (sHi == null)
						return;
					String sHf = JOptionPane.showInputDialog(null, "Hora fin (HH:mm o hh:mm AM/PM):");
					if (sHf == null)
						return;
					LocalTime hi, hf;
					try {
						hi = parseHoraFlexible(sHi);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						return;
					}
					try {
						hf = parseHoraFlexible(sHf);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						return;
					}
					cm.registrarDisponibilidad(Time.valueOf(hi), Time.valueOf(hf));
					JOptionPane.showMessageDialog(null, "Disponibilidad registrada");
				} else if (op.equals(ops[1])) { // modificar disponibilidad
					String sId = JOptionPane.showInputDialog(null, "Nro disponibilidad:");
					if (sId == null)
						return;
					int id = Integer.parseInt(sId.trim());
					String sHi = JOptionPane.showInputDialog(null, "Nueva hora inicio (HH:mm o hh:mm AM/PM):");
					if (sHi == null)
						return;
					String sHf = JOptionPane.showInputDialog(null, "Nueva hora fin (HH:mm o hh:mm AM/PM):");
					if (sHf == null)
						return;
					LocalTime hi, hf;
					try {
						hi = parseHoraFlexible(sHi);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						return;
					}
					try {
						hf = parseHoraFlexible(sHf);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						return;
					}
					cm.modificarDisponibilidad(id, Time.valueOf(hi), Time.valueOf(hf));
					JOptionPane.showMessageDialog(null, "Disponibilidad modificada");
				} else if (op.equals(ops[2])) { // listar disponibilidad
					var disps = cm.listarDisponibilidad();
					if (disps.isEmpty()) {
						JOptionPane.showMessageDialog(null, "No tienes disponibilidad cargada");
					} else {
						StringBuilder sb = new StringBuilder("Disponibilidad:\n");
						for (String d : disps)
							sb.append(d).append("\n");
						JTextArea ta = new JTextArea(sb.toString());
						ta.setEditable(false);
						JOptionPane.showMessageDialog(null, new JScrollPane(ta));
					}
				} else if (op.equals(ops[3])) { // eliminar disponibilidad
					String sId = JOptionPane.showInputDialog(null, "Nro de disponibilidad a eliminar:");
					if (sId == null)
						return;
					cm.eliminarDisponibilidad(Integer.parseInt(sId.trim()));
					JOptionPane.showMessageDialog(null, "Disponibilidad eliminada.");
				} else if (op.equals(ops[4])) { // visualizar agenda (rango)
					String sD1 = JOptionPane.showInputDialog(null, "Desde (YYYY-MM-DD):");
					if (sD1 == null)
						return;
					String sD2 = JOptionPane.showInputDialog(null, "Hasta (YYYY-MM-DD):");
					if (sD2 == null)
						return;
					LocalDate d1 = LocalDate.parse(sD1.trim(), DateTimeFormatter.ISO_DATE);
					LocalDate d2 = LocalDate.parse(sD2.trim(), DateTimeFormatter.ISO_DATE);
					var agenda = cm.visualizarAgenda(java.sql.Date.valueOf(d1), java.sql.Date.valueOf(d2));
					if (agenda.isEmpty()) {
						JOptionPane.showMessageDialog(null, "No hay turnos disponibles");
					} else {
						StringBuilder sb = new StringBuilder("Agenda:\n");
						agenda.forEach(d -> sb.append("- ").append(d).append("\n"));
						JTextArea ta = new JTextArea(sb.toString());
						ta.setEditable(false);
						JOptionPane.showMessageDialog(null, new JScrollPane(ta));
					}
				} else if (op.equals(ops[5])) { // proximos turnos
					String sLim = JOptionPane.showInputDialog(null, "¿Cuántos próximos turnos mostrar?");
					if (sLim == null)
						return;
					int lim = Integer.parseInt(sLim.trim());
					var prox = cm.proximosTurnos(lim);
					if (prox.isEmpty()) {
						JOptionPane.showMessageDialog(null, "Sin próximos turnos");
					} else {
						StringBuilder sb = new StringBuilder("Próximos turnos:\n");
						for (String s : prox)
							sb.append(s).append("\n");
						JTextArea ta = new JTextArea(sb.toString());
						ta.setEditable(false);
						JOptionPane.showMessageDialog(null, new JScrollPane(ta));
					}
				} else if (op.equals(ops[6])) { // Turnos del día
					String sDia = JOptionPane.showInputDialog(null, "Día (YYYY-MM-DD):");
					if (sDia == null)
						return;
					java.sql.Date dia = java.sql.Date.valueOf(sDia.trim());
					java.util.List<String> delDia = cm.turnosDelDia(dia);
					if (delDia.isEmpty()) {
						JOptionPane.showMessageDialog(null, "No hay turnos ese día");
					} else {
						StringBuilder sb = new StringBuilder("Turnos del día:\n");
						for (String s : delDia)
							sb.append(s).append("\n");
						JTextArea ta = new JTextArea(sb.toString());
						ta.setEditable(false);
						JOptionPane.showMessageDialog(null, new JScrollPane(ta));
					}
				} else if (op.equals(ops[7])) { // confirmar asistencia
					String sId = JOptionPane.showInputDialog(null, "Nro de turno a confirmar:");
					if (sId == null)
						return;
					cm.confirmarAsistencia(Long.parseLong(sId.trim()));
					JOptionPane.showMessageDialog(null, "Asistencia confirmada");
				} else if (op.equals(ops[8])) { // cancelar turno
					String sId = JOptionPane.showInputDialog(null, "Nro de turno a cancelar:");
					if (sId == null)
						return;
					cm.cancelarTurno(Long.parseLong(sId.trim()));
					JOptionPane.showMessageDialog(null, "Turno cancelado");
				} else if (op.equals(ops[9])) { // reprogramar turno
					String sId = JOptionPane.showInputDialog(null, "Nro de turno a reprogramar:");
					if (sId == null)
						return;
					String sFecha = JOptionPane.showInputDialog(null, "Nueva fecha (YYYY-MM-DD):");
					if (sFecha == null)
						return;
					String sHora = JOptionPane.showInputDialog(null, "Nueva hora (HH:mm o hh:mm AM/PM):");
					if (sHora == null)
						return;

					LocalDate fecha;
					try {
						fecha = LocalDate.parse(sFecha.trim(), DateTimeFormatter.ISO_DATE);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Fecha inválida");
						return;
					}

					LocalTime hora;
					try {
						hora = parseHoraFlexible(sHora);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						return;
					}

					var zdt = fecha.atTime(hora).atZone(java.time.ZoneId.systemDefault());
					Date nueva = Date.from(zdt.toInstant());
					cm.reprogramarTurno(Long.parseLong(sId.trim()), nueva);
					JOptionPane.showMessageDialog(null, "Turno reprogramado");
				} else if (op.equals(ops[10])) { // registrar consulta
					String userPac = JOptionPane.showInputDialog(null, "Nombre del paciente:");
					if (userPac == null)
						return;
					String motivo = JOptionPane.showInputDialog(null, "Motivo:");
					if (motivo == null)
						return;
					String diag = JOptionPane.showInputDialog(null, "Diagnóstico:");
					if (diag == null)
						return;
					String trat = JOptionPane.showInputDialog(null, "Tratamiento:");
					if (trat == null)
						return;
					String seg = JOptionPane.showInputDialog(null, "Recomendaciones:");
					if (seg == null)
						return;
					Consulta c = Consulta.hoy(motivo.trim(), diag.trim(), trat.trim(), seg.trim());
					long id = cm.registrarConsulta(userPac.trim(), c);
					JOptionPane.showMessageDialog(null, "Consulta registrada Nro = " + id);
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		
		
		
		else if (u instanceof Administrador a) {
		    ControllerAdministrador ca = new ControllerAdministrador(a);
		    String[] ops = {
		        "Registrar paciente",
		        "Modificar datos de paciente",
		        "Registrar médico",
		        "Modificar datos de médico",
		        "Listar usuarios",
		        "Eliminar usuario",
		        "Salir"
		    };

		    boolean salir = false;
		    while (!salir) {
		        Object choice = JOptionPane.showInputDialog(
		                null,
		                "Menú Administrador",
		                "Administrador",
		                JOptionPane.PLAIN_MESSAGE,
		                null,
		                ops,
		                ops[0]
		        );

		        if (choice == null || "Salir".equals(choice)) {
		            salir = true;
		            continue;
		        }

		        String op = choice.toString();

		        try {
		            switch (op) {
		                case "Registrar paciente" -> {
		                    String usr = JOptionPane.showInputDialog("Usuario:");
		                    String nom = JOptionPane.showInputDialog("Nombre:");
		                    String ape = JOptionPane.showInputDialog("Apellido:");
		                    String pass = JOptionPane.showInputDialog("Contraseña:");
		                    String nro = JOptionPane.showInputDialog("Número de contrato:");
		                    String os = JOptionPane.showInputDialog("Obra social:");

		                    if (usr != null && nom != null && ape != null && pass != null && nro != null && os != null) {
		                        ca.registrarPaciente(usr.trim(), nom.trim(), ape.trim(), pass.trim(),
		                                Integer.parseInt(nro.trim()), os.trim());
		                        JOptionPane.showMessageDialog(null, "Paciente registrado exitosamente");
		                    }
		                }
		                case "Modificar datos de paciente" -> {
		                    String usr = JOptionPane.showInputDialog("Usuario del paciente a modificar:");
		                    if (usr != null) {
		                        String nom = JOptionPane.showInputDialog("Nuevo nombre:");
		                        String ape = JOptionPane.showInputDialog("Nuevo apellido:");
		                        String pass = JOptionPane.showInputDialog("Nueva contraseña:");
		                        String nro = JOptionPane.showInputDialog("Nuevo número de contrato:");
		                        String os = JOptionPane.showInputDialog("Nueva obra social:");

		                        if (nom != null && ape != null && pass != null && nro != null && os != null) {
		                            ca.modificarPaciente(usr.trim(), nom.trim(), ape.trim(), pass.trim(),
		                                    Integer.parseInt(nro.trim()), os.trim());
		                            JOptionPane.showMessageDialog(null, "Paciente modificado exitosamente");
		                        }
		                    }
		                }
		                case "Registrar médico" -> {
		                    String usr = JOptionPane.showInputDialog("Usuario:");
		                    String nom = JOptionPane.showInputDialog("Nombre:");
		                    String ape = JOptionPane.showInputDialog("Apellido:");
		                    String pass = JOptionPane.showInputDialog("Contraseña:");
		                    String matricula = JOptionPane.showInputDialog("Matrícula:");
		                    String esp = JOptionPane.showInputDialog("Especialidad:");

		                    if (usr != null && nom != null && ape != null && pass != null && matricula != null && esp != null) {
		                        ca.registrarMedico(usr.trim(), nom.trim(), ape.trim(), pass.trim(),
		                                matricula.trim(), esp.trim());
		                        JOptionPane.showMessageDialog(null, "Médico registrado exitosamente");
		                    }
		                }
		                case "Modificar datos de médico" -> {
		                    String usr = JOptionPane.showInputDialog("Usuario del médico a modificar:");
		                    if (usr != null) {
		                        String nom = JOptionPane.showInputDialog("Nuevo nombre:");
		                        String ape = JOptionPane.showInputDialog("Nuevo apellido:");
		                        String pass = JOptionPane.showInputDialog("Nueva contraseña:");
		                        String matricula = JOptionPane.showInputDialog("Nueva matrícula:");
		                        String esp = JOptionPane.showInputDialog("Nueva especialidad:");

		                        if (nom != null && ape != null && pass != null && matricula != null && esp != null) {
		                            ca.modificarMedico(usr.trim(), nom.trim(), ape.trim(), pass.trim(),
		                                    matricula.trim(), esp.trim());
		                            JOptionPane.showMessageDialog(null, "Médico modificado exitosamente");
		                        }
		                    }
		                }
		                case "Listar usuarios" -> {
		                    String rol = JOptionPane.showInputDialog("Ingrese rol a listar (Paciente o Medico):");
		                    if (rol != null) {
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
		                }
		                case "Eliminar usuario" -> {
		                    String usr = JOptionPane.showInputDialog("Usuario a eliminar:");
		                    if (usr != null) {
		                        ca.eliminarUsuario(usr.trim());
		                        JOptionPane.showMessageDialog(null, "Usuario eliminado exitosamente");
		                    }
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