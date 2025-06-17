import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';

interface Props {
  text: string;
  children: React.ReactNode;
  showTooltip?: boolean;
  tooltipSide?: 'top' | 'bottom' | 'left' | 'right';
}

const TruncateWithTooltip: React.FC<Props> = ({
  text,
  children,
  showTooltip = true,
  tooltipSide = 'top',
}) => {
  return (
    <Tooltip>
      <TooltipTrigger asChild>{children}</TooltipTrigger>
      {showTooltip && (
        <TooltipContent side={tooltipSide} className='max-w-xs p-2 text-sm break-words'>
          {text}
        </TooltipContent>
      )}
    </Tooltip>
  );
};

export default TruncateWithTooltip;
