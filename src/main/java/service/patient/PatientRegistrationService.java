package service.patient;

import exception.ServerTechnicalProblemsException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import exception.AlreadyExistsException;
import exception.DtoValidationException;
import service.mapper.PatientDtoMapper;
import servlet.response.PatientRegistrationResponse;
import service.dto.patient.PatientDto;
import service.dto.patient.PatientRegistrationDto;
import entity.Patient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import repository.PatientRepository;
import service.mapper.PatientMapper;
import servlet.converter.request.RegistrationPatientConverter;
import service.dto.validator.DtoValidator;
import util.SessionPool;

import java.io.IOException;
import java.io.PrintWriter;

import static jakarta.servlet.http.HttpServletResponse.*;

@RequiredArgsConstructor
public class PatientRegistrationService {

    private final PatientRepository patientRepository;

    private final RegistrationPatientConverter registrationPatientConverter;
    private final PatientMapper patientMapper;
    private final PatientDtoMapper patientDtoMapper;

    private final DtoValidator dtoValidator;

    @SneakyThrows
    public PatientDto registration(PrintWriter writer, HttpServletRequest request, HttpServletResponse response) throws AlreadyExistsException, IOException, ServerTechnicalProblemsException, DtoValidationException {
        Session session = SessionPool.getSession();
        Patient patient = createPatient(request);
        try {
            session.beginTransaction();
            if(patientRepository.registerPatient(patient, session)){
                Patient registeredPatient = patientRepository.findByLogin(patient.getLogin(), session).get();
                session.getTransaction().commit();
                return patientDtoMapper.mapFrom(registeredPatient);

                registrationResponse.send(response.getWriter(), response, patientDto, SC_CREATED);
            }
        }catch (Exception exception){
            session.getTransaction().rollback();
            throw exception;
        }


             else throw new AlreadyExistsException();
    }

    private Patient createPatient(HttpServletRequest request) throws IOException, DtoValidationException {
        Patient patient = new Patient();
        PatientRegistrationDto registrationDto = registrationPatientConverter.convert(request);
        if (dtoValidator.isValid(registrationDto)) {
            return patientMapper.mapFrom(registrationDto);
        }
        return patient;
    }
}