package main.services;

import main.model.repositories.CaptchaRepository;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.annotation.Autowired;

public class CaptchaRepositoryServiceTest {


    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private CaptchaRepository captchaRepository;


}
