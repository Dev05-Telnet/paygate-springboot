import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getEntity } from './user-data.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const UserDataDetail = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getEntity(props.match.params.id));
  }, []);

  const userDataEntity = useAppSelector(state => state.userData.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="userDataDetailsHeading">UserData</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{userDataEntity.id}</dd>
          <dt>
            <span id="userId">User Id</span>
          </dt>
          <dd>{userDataEntity.userId}</dd>
          <dt>
            <span id="store">Store</span>
          </dt>
          <dd>{userDataEntity.store}</dd>
          <dt>
            <span id="token">Token</span>
          </dt>
          <dd>{userDataEntity.token}</dd>
          <dt>
            <span id="payGateID">Pay Gate ID</span>
          </dt>
          <dd>{userDataEntity.payGateID}</dd>
          <dt>
            <span id="payGateSecret">Pay Gate Secret</span>
          </dt>
          <dd>{userDataEntity.payGateSecret}</dd>
        </dl>
        <Button tag={Link} to="/user-data" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/user-data/${userDataEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default UserDataDetail;
