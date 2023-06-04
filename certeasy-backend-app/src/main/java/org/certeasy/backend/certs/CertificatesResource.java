package org.certeasy.backend.certs;


import org.certeasy.Certificate;
import org.certeasy.KeyStrength;
import org.certeasy.backend.common.BaseResource;
import org.certeasy.backend.common.SubCaSpec;
import org.certeasy.certspec.CertificateAuthoritySpec;
import org.certeasy.certspec.CertificateAuthoritySubject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/issuers/{issuerId}/certificates")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CertificatesResource extends BaseResource {

    @GET
    public Response list(@PathParam("issuerId") String issuerId){
        return this.checkIssuerExistsThen(issuerId, (issuer) -> {
            //TODO: Implement: Return List of CertSummaryInfo
            throw new UnsupportedOperationException();
        });
    }

    @POST
    @Path("/tls-server")
    public Response issueTLSServerCertificate(@PathParam("issuerId") String issuerId, ServerSpec spec){
        return this.checkIssuerExistsThen(issuerId, (issuer) -> {
            //TODO: Implement
            throw new UnsupportedOperationException();
        });
    }

    @POST
    @Path("/sub-ca")
    public Response issueSubCaCertificate(@PathParam("issuerId") String issuerId, SubCaSpec spec){
        return this.checkIssuerExistsThen(issuerId, (issuer) -> {
            CertificateAuthoritySubject subCaSubject = new CertificateAuthoritySubject(
                    spec.getName(),
                    spec.getGeographicAddressInfo().
                            toGeographicAddress());
            CertificateAuthoritySpec subAuthoritySpec = new CertificateAuthoritySpec(subCaSubject, spec.getPathLength(),
                    KeyStrength.valueOf(spec.getKeyStrength()),
                    spec.getValidity().toDateRange());
            Certificate certificate = issuer.issueCert(subAuthoritySpec);
            return Response.ok(new IssuedCert(certificate.getSerial()))
                    .build();
        });
    }

    @GET
    @Path("/certificates/{serial}/files")
    public Response downloadCertFiles(@PathParam("issuerId") String issuerId, @PathParam("serial") String serial){

        //TODO: Implement
        throw new UnsupportedOperationException();

    }

}
