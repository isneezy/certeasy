package org.certeasy.rest;

import org.certeasy.*;
import org.certeasy.bouncycastle.BouncyCastleCertGenerator;
import org.certeasy.bouncycastle.BouncyCastlePEMCoder;
import org.certeasy.certspec.*;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;

public class Start {

    public static void main(String[] args){

        GeographicAddress address = new GeographicAddress("MZ", "Maputo", "Boane", "Belo Horizonte, Rua das Flores, Casa 48");
        CertificateAuthoritySubject authoritySubject = new CertificateAuthoritySubject("MyCA", address);
        CertificateAuthoritySpec authoritySpec = new CertificateAuthoritySpec(authoritySubject, KeyStrength.MEDIUM_STRENGTH,
                new DateRange(LocalDate.of(2099, 12, 12)));

        CertificateGenerator generator = new BouncyCastleCertGenerator();
        Certificate authorityCert =  generator.generate(authoritySpec);
        authorityCert.exportDER(new File("example-ca.cer"));

        PersonalIdentitySubject personalIdentitySubject = new PersonalIdentitySubject(
                new PersonName("Mario Joao Francisco", "Junior"),
                address, "+258842538083", Set.of("francisco.junior.mario@gmail.com"),
                Set.of("talkcode", "mariusclub")
        );

        PersonalCertificateSpec personalCertificateSpec = new PersonalCertificateSpec(personalIdentitySubject,
                KeyStrength.HIGH_STRENGTH,
                new DateRange(LocalDate.of(2023,12,11)));

        Certificate personalCert = generator.generate(personalCertificateSpec, authorityCert);
        personalCert.exportDER(new File("mario.cer"));

        EmployeeIdentitySubject employeeIdentitySubject = new EmployeeIdentitySubject(
                new PersonName("Mario Joao Francisco", "Junior"),
                address, "+258849902228", "mario.franciscojunior@vm.co.mz", "franciscojuniorjum",
                new OrganizationBinding("Vodacom Mocambique SA", "Technology", "Manager: Software Development"));

        CertificateSpec employeeCertSpec = new EmployeeCertificateSpec(
                employeeIdentitySubject,KeyStrength.MEDIUM_STRENGTH,
                new DateRange(LocalDate.of(2023,12,11)));

        Certificate employeeCert = generator.generate(employeeCertSpec, authorityCert);
        employeeCert.exportDER(new File("mario-vm.cer"));

        PEMCoder pemCoder = new BouncyCastlePEMCoder();
        String privatePem = pemCoder.encodePrivateKey(employeeCert);
        String certPem = pemCoder.encodeCert(employeeCert);

        Certificate decoded = pemCoder.decodeCertificate(certPem, privatePem);

    }

}
