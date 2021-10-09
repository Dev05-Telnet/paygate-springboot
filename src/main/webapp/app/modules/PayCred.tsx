import 'react-toastify/dist/ReactToastify.css';
import '../app.scss';
import 'app/config/dayjs.ts';

import React, { useState } from 'react';
import { hot } from 'react-hot-loader';
import BgContainer from '../components/BgContainer';
import WhiteContainer from '../components/WhiteContainer';
import { AvForm, AvField } from 'availity-reactstrap-validation';
import { Button } from 'reactstrap';
import axios from 'axios';
import { toast, ToastContainer } from 'react-toastify';
import { useParams } from 'react-router-dom';

const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

export const PayCred = () => {
  const [paygateStatus, setPaygateStatus] = useState('Not configured');
  const userId = useParams();
  const click = async (event, values) => {
    const response = await axios.post(
      `/api/update/${userId['userId']}?paygateId=${values.paygateId}&paygateSecret=${values.paygateSecret}`
    );
    // eslint-disable-next-line no-console
    if (response.status === 200) {
      setPaygateStatus('Configured');
      toast.success(response.data);
    } else toast.error('Something went wrong!');
  };
  const url = new URL(window.location.href);
  const payId = url.searchParams.get('id');
  const secret = url.searchParams.get('secret');
  return (
    <div>
      <BgContainer>
        <WhiteContainer>
          <p>Paygate configuration Status : {payId ? 'Configured' : paygateStatus}</p>
          <AvForm onValidSubmit={click}>
            <AvField name="paygateId" label="Paygate Id" required defaultValue={payId} />
            <AvField name="paygateSecret" label="Paygate Secret" required defaultValue={secret} />
            <Button>Submit</Button>
          </AvForm>
        </WhiteContainer>
      </BgContainer>
    </div>
  );
};

export default PayCred;
