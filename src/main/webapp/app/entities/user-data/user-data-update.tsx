import React, { useState, useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getEntity, updateEntity, createEntity, reset } from './user-data.reducer';
import { IUserData } from 'app/shared/model/user-data.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const UserDataUpdate = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const userDataEntity = useAppSelector(state => state.userData.entity);
  const loading = useAppSelector(state => state.userData.loading);
  const updating = useAppSelector(state => state.userData.updating);
  const updateSuccess = useAppSelector(state => state.userData.updateSuccess);

  const handleClose = () => {
    props.history.push('/user-data');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(props.match.params.id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...userDataEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...userDataEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="payGateApp.userData.home.createOrEditLabel" data-cy="UserDataCreateUpdateHeading">
            Create or edit a UserData
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="user-data-id"
                  label="ID"
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField label="User Id" id="user-data-userId" name="userId" data-cy="userId" type="text" />
              <ValidatedField label="Store" id="user-data-store" name="store" data-cy="store" type="text" />
              <ValidatedField label="Token" id="user-data-token" name="token" data-cy="token" type="text" />
              <ValidatedField
                label="Pay Gate ID"
                id="user-data-payGateID"
                name="payGateID"
                data-cy="payGateID"
                type="text"
              />
              <ValidatedField
                label="Pay Gate Secret"
                id="user-data-payGateSecret"
                name="payGateSecret"
                data-cy="payGateSecret"
                type="text"
              />
              <ValidatedField
                label="Script Id"
                id="user-data-scriptId"
                name="scriptId"
                data-cy="scriptId"
                type="text"
              />
              <Button
                tag={Link}
                id="cancel-save"
                data-cy="entityCreateCancelButton"
                to="/user-data"
                replace
                color="info"
              >
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button
                color="primary"
                id="save-entity"
                data-cy="entityCreateSaveButton"
                type="submit"
                disabled={updating}
              >
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default UserDataUpdate;
