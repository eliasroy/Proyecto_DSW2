package com.epy.main.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.epy.main.dto.AutenticacionDTO;
import com.epy.main.dto.AutenticacionResponseDTO;
import com.epy.main.dto.FiltroPersonaDTO;
import com.epy.main.dto.PersonaDTO;
import com.epy.main.dto.RptaServerDTO;
import com.epy.main.dto.UsuarioDTO;
import com.epy.main.entity.Authority;
import com.epy.main.entity.Persona;
import com.epy.main.entity.User;
import com.epy.main.service.UserService;
import com.epy.main.service.PersonaService;
@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = "http://localhost:4200")
public class RestUsuarioController {
	
	@Autowired
	private UserService serviceUsuario;
	@Autowired
	PersonaService servicePersona;
	
	@GetMapping
	@ResponseBody
	public ResponseEntity<List<PersonaDTO>> listadoUsuarios() {
		return ResponseEntity.ok(servicePersona.listadoPersona());
	}
	
	@PostMapping("/listadoFiltro")
	@ResponseBody
	public ResponseEntity<List<PersonaDTO>> listadoFiltro(@RequestBody FiltroPersonaDTO dto){
		List<PersonaDTO> lista = new ArrayList<>();
		if (dto.getTipoUsuario() == 0 && dto.getDni().equals("")) {
			lista = servicePersona.findConsultaUsuarioxApellido("%" + dto.getApellidos() + "%");
		} else {
			lista = servicePersona.findConsultaUsuario(dto.getTipoUsuario(), dto.getDni());
		}
		return ResponseEntity.ok(lista);
	}

	
	/*
	@PostMapping
	@ResponseBody
	public  ResponseEntity<Map<String, Object>> insertaUser(@RequestBody Persona obj){
		Map<String, Object> salida = new HashMap<>();
		
		try {
			Date date = new Date();
			SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
			List<Persona> listaPersona=servicePersona.buscarDni(obj.getDni());
			
			if (listaPersona.size()  == 0) {
				Persona persona = new Persona();
				//persona.setIdpersona(obj.getIdpersona());
				persona.setNombre(obj.getNombre());
				persona.setApellidos(obj.getApellidos());
				persona.setDni(obj.getDni());
				persona.setCelular(obj.getCelular());
				persona.setTelefono(obj.getTelefono());
				persona.setFechaRegistro(formato.format(date));
				
				Optional<User> usuarioBusqueda = serviceUsuario.buscarUsuario(obj.getDni());
				
				User user = new User();
				if (usuarioBusqueda.isPresent()) {
					user.setId(usuarioBusqueda.get().getId());
					user.setPassword(usuarioBusqueda.get().getPassword());
					user.setUsername(usuarioBusqueda.get().getPersona().getDni());
				} else {
					user.setPassword(obj.getDni());
					user.setUsername(obj.getDni());
				}
				
				Authority objAuthority = new Authority();
				objAuthority.setId(1);
				user.setEnabled(true);
				user.setAuthority(objAuthority);
				user.setPersona(persona);
				persona.setUser(user);
				
				int rptaGuardar = servicePersona.guardar(persona);
				serviceUsuario.guardar(user);

				if (rptaGuardar > 0) {
					salida.put("mensaje", "REGISTRO EXITOSO");
				} else {
					salida.put("mensaje", "error en el registro");
				}
						
			}else {
				salida.put("mensaje", "EL USUARIO YA EXISTE DNI:"+ obj.getDni());	
			}
		} catch (Exception e) {
			e.printStackTrace();
			salida.put("mensaje", "error en el registro "+e.getMessage());
		}
		return ResponseEntity.ok(salida);
	}
	*/

	@GetMapping("/listaPersonas")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> listaProveedorConParametros(
			@RequestParam(name="tipoUsuario", required = true, defaultValue = "1") int tipoUsuario,
			@RequestParam(name ="dni", required = false, defaultValue = "") String dni,
			@RequestParam(name ="apellidos", required = false, defaultValue = "") String apellidos
			){
		
		System.out.println("listaPersonas - tipoUsuario : " + tipoUsuario);
		System.out.println("listaPersonas - dni : " + dni);
		System.out.println("listaPersonas - apellidos : " + apellidos);
		
		Map<String, Object> salida = new HashMap<>();
		try {
			List<PersonaDTO> lista = servicePersona.busquedaPersonas(tipoUsuario,dni,"%"+apellidos+"%");
			if (CollectionUtils.isEmpty(lista)) {
				salida.put("mensaje", "No existen registros para mostrar." );
			}else{
				salida.put("lista", lista);
				salida.put("mensaje", "Existen " + lista.size() + " registros para mostrar.");
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			salida.put("mensaje", "No se registro, ocurrio un error.");
		}
		return ResponseEntity.ok(salida);
	}

	@PostMapping("/autenticarUsuario")
	public ResponseEntity<AutenticacionResponseDTO> getValidarUsuario(@RequestBody AutenticacionDTO obj){
		System.out.println("api-autenticar-Usuario : " + obj.getUsername());
		System.out.println("api-autenticar-Clave : " + obj.getPassword());
		AutenticacionResponseDTO objRpta = new AutenticacionResponseDTO();
		System.out.println("Contra: " + obj.getPassword());
		UsuarioDTO usuario = serviceUsuario.findByUsernameAndPassword(obj.getUsername(), obj.getPassword());
		
		if (usuario != null) {
			objRpta.setCodigo("1");
			objRpta.setMensaje("Login Correcto");
		}else {
			objRpta.setCodigo("0");
			objRpta.setMensaje("Login Incorrecto");
		}
		return ResponseEntity.ok(objRpta);
	}
	
	
	
	@PostMapping
	@ResponseBody
	public  ResponseEntity<RptaServerDTO> insertaUser(@RequestBody PersonaDTO obj){
		RptaServerDTO rpta = new RptaServerDTO();
		
		System.out.println("insertaUser -codigo : " + obj.getCodigo());
		System.out.println("insertaUser -nombre : " + obj.getNombre());
		System.out.println("insertaUser -apellido : " + obj.getApellido());
		System.out.println("insertaUser -documento : " + obj.getDocumento());
		System.out.println("insertaUser -tipoUsuario : " + obj.getTipoUsuario());
		
		try {
			
			Date date = new Date();
			SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
			List<Persona> listaPersona=servicePersona.buscarDni(obj.getDocumento());
			
			if (listaPersona.size()  == 0) {
				Persona persona = new Persona();
				//persona.setIdpersona(obj.getIdpersona());
				persona.setNombre(obj.getNombre());
				persona.setApellidos(obj.getApellido());
				persona.setDni(obj.getDocumento());
				persona.setCelular(obj.getCelular());
				persona.setTelefono(obj.getTelefono());
				persona.setFechaRegistro(formato.format(date));
				
				Optional<User> usuarioBusqueda = serviceUsuario.buscarUsuario(obj.getDocumento());
				
				User user = new User();
				if (usuarioBusqueda.isPresent()) {
					user.setId(usuarioBusqueda.get().getId());
					user.setPassword(usuarioBusqueda.get().getPassword());
					user.setUsername(usuarioBusqueda.get().getPersona().getDni());
				} else {
					user.setPassword(obj.getDocumento());
					user.setUsername(obj.getDocumento());
				}
				
				Authority objAuthority = new Authority();
				objAuthority.setId(Integer.parseInt(obj.getTipoUsuario()));//objAuthority.setId(1);
				user.setEnabled(true);
				user.setAuthority(objAuthority);
				user.setPersona(persona);
				persona.setUser(user);
				
				int rptaGuardar = servicePersona.guardar(persona);
				serviceUsuario.guardar(user);

				if (rptaGuardar > 0) {
					rpta.setCodigo(1);
					rpta.setMensaje("REGISTRO EXITOSO");
				} else {
					rpta.setCodigo(2);
					rpta.setMensaje("NO SE REGISTRO");
				}
			}else {
				rpta.setCodigo(3);
				rpta.setMensaje("EL USUARIO YA EXISTE DNI:"+ obj.getDocumento());
			}
		} catch (Exception e) {
			e.printStackTrace();
			rpta.setCodigo(-1);
			rpta.setMensaje("Error");
		}
		
		System.out.println(rpta.getCodigo());
		System.out.println(rpta.getMensaje());
		return ResponseEntity.ok(rpta);
	}
	
	
	@PostMapping("/actualizarUsuario")
	@ResponseBody
	public  ResponseEntity<RptaServerDTO> actualizarUsuario(@RequestBody PersonaDTO obj){
		RptaServerDTO rpta = new RptaServerDTO();
		
		System.out.println("insertaUser -codigo : " + obj.getCodigo());
		System.out.println("insertaUser -nombre : " + obj.getNombre());
		System.out.println("insertaUser -apellido : " + obj.getApellido());
		System.out.println("insertaUser -documento : " + obj.getDocumento());
		System.out.println("insertaUser -celular : " + obj.getCelular());
		System.out.println("insertaUser -telefono : " + obj.getTelefono());
		System.out.println("insertaUser -tipoUsuario : " + obj.getTipoUsuario());
		
		try {
			List<PersonaDTO> listaPersona=servicePersona.buscarxDocumento(obj.getDocumento());
			if (listaPersona.size() > 0) {
				System.out.println("Nombre : " + listaPersona.get(0).getNombre());
				Persona persona = new Persona();
				persona.setIdpersona(obj.getCodigo());
				persona.setNombre(obj.getNombre());
				persona.setApellidos(obj.getApellido());
				persona.setDni(obj.getDocumento());
				persona.setCelular(obj.getCelular());
				persona.setTelefono(obj.getTelefono());
				int rptaGuardar = servicePersona.guardar(persona);
				if (rptaGuardar > 0) {
					rpta.setCodigo(1);
					rpta.setMensaje("ACTUALIZACION EXITOSO");
				} else {
					rpta.setCodigo(2);
					rpta.setMensaje("NO SE ACTUALIZO");
				}
			}else {
				rpta.setCodigo(3);
				rpta.setMensaje("EL USUARIO NO EXISTE : "+ obj.getDocumento());
			}
		} catch (Exception e) {
			e.printStackTrace();
			rpta.setCodigo(-1);
			rpta.setMensaje("Error");
		}
		System.out.println(rpta.getCodigo());
		System.out.println(rpta.getMensaje());
		return ResponseEntity.ok(rpta);
	}
	
	
	
	

	
	
	
	
	

}
