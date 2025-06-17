import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMemo, useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Portal } from '@radix-ui/react-portal';
import { InputField } from '@/components/form/InputField';
import { DateField } from '@/components/form/DateField';
import { RadioGroupField, type RadioOption } from '@/components/form/RadioGroupField';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import { useGetAssetBasic, usePatchAsset } from '@/hooks/fetch/assets';
import { RANGE } from '@/configs/constants';
import { EditAssetFormRequest, editAssetSchema } from '@/utils/form/schemas/edit-asset-schema';
import Spinner from '@/components/Spinner';
import useModal from '@/hooks/useModal';
import { toast } from 'react-toastify';
import { Asset, AssetStateHandler } from '@/types/asset';
import ReloadModal from '@/components/modal/ReloadModal';

const UpdateAssetPage = () => {
  const { assetId } = useParams();
  const { data: asset, error } = useGetAssetBasic(assetId!);
  const [scenario, setScenario] = useState(0);
  useEffect(() => {
    if (error) {
      setScenario(2);
    }
  }, [error]);

  useEffect(() => {
    if (asset) {
      setScenario(1);
    }
  }, [asset]);

  return (
    <div className='flex w-full items-center justify-center'>
      {scenario === 0 && <Loading />}
      {scenario === 1 && <UpdateAssetForm id={assetId!} asset={asset!} />}
      {scenario === 2 && error && <UnavailableToUpdate />}
      {scenario === 3 && (
        <ErrorFallback error={error?.message || 'An error occurred while fetching the asset.'} />
      )}
    </div>
  );
};

interface UpdateAssetFormProps {
  id: string;
  asset: Asset<'basic'>;
}

const UpdateAssetForm: React.FC<UpdateAssetFormProps> = ({ id, asset }) => {
  const navigate = useNavigate();
  const { isModalVisible, openModal, closeModal } = useModal(false);
  const [modal, setModal] = useState(0);
  const { triggerUpdate, updateError, isUpdating } = usePatchAsset(id);
  const form = useForm<EditAssetFormRequest>({
    resolver: zodResolver(editAssetSchema),
    defaultValues: {
      name: asset.name,
      categoryName: asset.category.name,
      specification: asset.specification,
      installedDate: asset.installedDate,
      state: asset.state,
      version: asset.version,
    },
    mode: 'onChange',
  });

  const isFormValid = form.formState.isValid;

  const assetStateRadioItems: RadioOption[] = useMemo(() => {
    return AssetStateHandler.getOptions('edit');
  }, []);

  const onSubmit = async (data: EditAssetFormRequest) => {
    const updatedAsset = await triggerUpdate(data);
    if (updatedAsset) {
      toast.success(`Asset ${data.name} updated successfully.`);
      closeModal();
      setModal(0);
      navigate('/assets', { replace: true, state: { updatedAsset: updatedAsset } });
      return;
    }
  };

  useEffect(() => {
    if (updateError) {
      switch (updateError.code) {
        case 'ASSET_NOT_FOUND':
          setModal(2);
          break;
        case 'ASSET_NOT_EDITABLE':
          setModal(2);
          break;
        case 'ASSET_BEING_MODIFIED':
          setModal(2);
          break;
        default:
          toast.error('An error occurred while updating the asset.');
      }
    }
  }, [updateError]);

  return (
    <>
      <div className='mx-auto rounded-lg bg-white p-6 shadow-sm md:min-w-128'>
        <h1 className='text-primary mb-5 text-2xl font-bold'>Edit Asset</h1>
        <Form {...form}>
          <form className='space-y-4'>
            <InputField
              maxLength={RANGE.ASSET_NAME.MAX}
              label='Name'
              name='name'
              control={form.control}
            />
            <InputField name='categoryName' label='Category' control={form.control} disabled />

            <InputField
              name='specification'
              label='Specification'
              control={form.control}
              maxLength={RANGE.ASSET_SPECIFICATION.MAX}
              multiline={true}
              rows={5}
            />

            <DateField
              name='installedDate'
              label='Installed Date'
              control={form.control}
              max={new Date().toISOString().split('T')[0]}
            />

            <RadioGroupField
              name='state'
              label='State'
              control={form.control}
              options={assetStateRadioItems}
            />

            <div className='flex justify-end gap-4 pt-4'>
              <Button
                type='button'
                disabled={!isFormValid || isUpdating}
                className='bg-primary text-white'
                onClick={() => {
                  setModal(1);
                  openModal();
                }}
              >
                {isUpdating && <Spinner />}
                <span>Save</span>
              </Button>
              <Button type='button' variant='outline' onClick={() => navigate(-1)}>
                Cancel
              </Button>
            </div>
          </form>
        </Form>
      </div>

      {/* Confirmation Modal */}
      {isModalVisible && modal === 1 && (
        <Dialog open={isModalVisible} onOpenChange={openModal}>
          <Portal>
            <div className='fixed inset-0 z-40 bg-black/50' onClick={closeModal} />
            <DialogContent className='fixed top-1/2 left-1/2 z-50 w-96 -translate-x-1/2 -translate-y-1/2 rounded-lg bg-white p-6'>
              <DialogHeader>
                <DialogTitle>Are you sure?</DialogTitle>
              </DialogHeader>
              <p className='mb-4'>Your changes will be applied to the asset.</p>
              <div className='flex justify-end gap-3'>
                <Button onClick={closeModal} variant='outline'>
                  Cancel
                </Button>
                <Button onClick={form.handleSubmit(onSubmit)} disabled={isUpdating}>
                  {isUpdating ? (
                    <span className='flex gap-1'>
                      <Spinner /> Confirm
                    </span>
                  ) : (
                    'Confirm'
                  )}
                </Button>
              </div>
            </DialogContent>
          </Portal>
        </Dialog>
      )}
      {isModalVisible && modal === 2 && (
        <ReloadModal visible={isModalVisible} openModal={openModal} closeModal={closeModal} />
      )}
    </>
  );
};

const Loading = () => (
  <div className='mx-auto min-h-100 max-w-md rounded-lg bg-white p-6 shadow-sm'>
    <h1 className='text-primary mb-5 text-2xl font-bold'>Loading...</h1>
  </div>
);

const ErrorFallback = ({ error }: { error: string }) => (
  <div className='mx-auto max-w-md rounded-lg bg-white p-6 shadow-sm'>
    <h1 className='text-primary mb-5 text-2xl font-bold'>{error}</h1>
  </div>
);

const UnavailableToUpdate = () => {
  return (
    <div>
      <h1 className='text-primary mb-5 text-2xl font-bold'>Asset Not Available for Update</h1>
      <p className='text-gray-600'>This asset is either not available or already assigned.</p>
      <Link to='/assets'>Click here to return to asset management page</Link>
    </div>
  );
};

export default UpdateAssetPage;
